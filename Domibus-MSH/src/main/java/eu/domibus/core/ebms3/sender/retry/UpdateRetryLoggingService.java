package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.*;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UpdateRetryLoggingService {

    public static final String DELETE_PAYLOAD_ON_SEND_FAILURE = DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD;
    public static final String MESSAGE_EXPIRATION_DELAY = DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UpdateRetryLoggingService.class);

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected MessageAttemptService messageAttemptService;

    @Autowired
    protected PModeProvider pModeProvider;


    /**
     * This method is responsible for the handling of retries for a given sent message.
     * In case of failure the message will be put back in waiting_for_retry status, after a certain amount of retry/time
     * it will be marked as failed.
     *
     * @param messageId        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePushedMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration, final MessageAttempt messageAttempt) {
        updateRetryLogging(messageId, legConfiguration, MessageStatus.WAITING_FOR_RETRY, messageAttempt);
    }

    /**
     * Set a message as failed if it has expired
     *
     * @param userMessage The userMessage to be checked for expiration
     * @return true in case the message was set as expired
     * @throws EbMS3Exception If the LegConfiguration could not found
     */
    @Transactional
    public boolean failIfExpired(UserMessage userMessage) throws EbMS3Exception {
        final String messageId = userMessage.getMessageInfo().getMessageId();
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);

        final String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        LOG.debug("PMode key found : [{}]", pModeKey);
        LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

        boolean expired = isExpired(legConfiguration, userMessageLog);
        if (!expired) {
            LOG.debug("Message [{}] is not expired", messageId);
            return false;
        }
        LOG.debug("Message [{}] is expired", messageId);
        messageFailed(userMessage, userMessageLog);

        if (userMessage.isUserMessageFragment()) {
            userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId(), String.format("Message fragment [%s] has failed to be sent", messageId));

        }
        return true;

    }


    protected void updateRetryLogging(final String messageId, final LegConfiguration legConfiguration, MessageStatus messageStatus, final MessageAttempt messageAttempt) {
        LOG.debug("Updating retry for message");
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
        LOG.debug("Updating sendAttempts to [{}]", userMessageLog.getSendAttempts());
        userMessageLog.setNextAttempt(getScheduledStartDate(userMessageLog)); // this is needed for the first computation of "next attempt" if receiver is down

        userMessageLog.setScheduled(false);
        LOG.debug("Scheduled flag for message [{}] has been reset to false", messageId);

        userMessageLogDao.update(userMessageLog);
        if (hasAttemptsLeft(userMessageLog, legConfiguration) && !userMessageLog.isTestMessage()) {
            updateNextAttemptAndNotify(legConfiguration, messageStatus, userMessageLog);
        } else { // max retries reached, mark message as ultimately failed (the message may be pushed back to the send queue by an administrator but this send completely failed)
            final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
            messageFailed(userMessage, userMessageLog);

            if (userMessage.isUserMessageFragment()) {
                userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId(), String.format("Message fragment [%s] has failed to be sent", messageId));
            }
        }
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());

        if (messageAttempt != null) {
            messageAttemptService.createAndUpdateEndDate(messageAttempt);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void messageFailedInANewTransaction(UserMessage userMessage, UserMessageLog userMessageLog, final MessageAttempt messageAttempt) {
        LOG.debug("Marking message [{}] as failed in a new transaction", userMessage.getMessageInfo().getMessageId());

        messageFailed(userMessage, userMessageLog);
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getMessageId());
    }

    public void messageFailed(UserMessage userMessage, UserMessageLog userMessageLog) {
        LOG.debug("Marking message [{}] as failed", userMessage.getMessageInfo().getMessageId());

        NotificationStatus notificationStatus = userMessageLog.getNotificationStatus();
        boolean isTestMessage = userMessageLog.isTestMessage();

        LOG.businessError(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_FAILURE : DomibusMessageCode.BUS_MESSAGE_SEND_FAILURE,
                userMessage.getFromFirstPartyId(), userMessage.getToFirstPartyId());
        if (NotificationStatus.REQUIRED.equals(notificationStatus) && !isTestMessage) {
            LOG.info("Notifying backend for message failure");
            backendNotificationService.notifyOfSendFailure(userMessageLog);
        }

        userMessageLogService.setMessageAsSendFailure(userMessage, userMessageLog);

        if (shouldDeletePayloadOnSendFailure(userMessage)) {
            messagingDao.clearPayloadData(userMessage);
        }
    }

    protected boolean shouldDeletePayloadOnSendFailure(UserMessage userMessage) {
        if (userMessage.isUserMessageFragment()) {
            return true;
        }
        return domibusPropertyProvider.getBooleanProperty(DELETE_PAYLOAD_ON_SEND_FAILURE);
    }


    @Transactional
    public void updateWaitingReceiptMessageRetryLogging(final String messageId, final LegConfiguration legConfiguration) {
        LOG.debug("Updating waiting receipt retry for message");
        updateRetryLogging(messageId, legConfiguration, MessageStatus.WAITING_FOR_RECEIPT, null);
    }

    public void updateNextAttemptAndNotify(LegConfiguration legConfiguration, MessageStatus messageStatus, UserMessageLog userMessageLog) {
        updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        saveAndNotify(messageStatus, userMessageLog);
    }

    public void saveAndNotify(MessageStatus messageStatus, UserMessageLog userMessageLog) {
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, messageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(messageStatus);
        LOG.debug("Updating status to [{}]", userMessageLog.getMessageStatus());
        userMessageLogDao.update(userMessageLog);

    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     *
     * @param userMessageLog   the message to check
     * @param legConfiguration processing information for the message
     * @return true if the message can be sent again
     */
    public boolean hasAttemptsLeft(final MessageLog userMessageLog, final LegConfiguration legConfiguration) {
        if (legConfiguration == null) {
            LOG.debug("No more send attempts as leg configuration is not found.");
            return false;
        }
        if (legConfiguration.getReceptionAwareness() == null) {
            LOG.debug("No more send attempts as reception awareness of the leg configuration is not null.");
            return false;
        }
        LOG.debug("Send attempts [{}], max send attempts [{}], scheduled start time [{}], retry timeout [{}]",
                userMessageLog.getSendAttempts(), userMessageLog.getSendAttemptsMax(),
                getScheduledStartTime(userMessageLog), legConfiguration.getReceptionAwareness().getRetryTimeout());
        // retries start after the first send attempt
        Boolean hasMoreAttempts = userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax();
        long retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000L;
        Boolean hasMoreTime = (getScheduledStartTime(userMessageLog) + retryTimeout) > System.currentTimeMillis();

        LOG.debug("Verify if has more attempts: [{}] and has more time: [{}]", hasMoreAttempts, hasMoreTime);
        return hasMoreAttempts && hasMoreTime;
    }

    /**
     * Gets the scheduled start date of the message: if the message has been restored is returns the restored date otherwise it returns the received date
     *
     * @param userMessageLog the message
     * @return the scheduled start date in milliseconds elapsed since the UNIX epoch
     */
    public Long getScheduledStartTime(final MessageLog userMessageLog) {
        return getScheduledStartDate(userMessageLog).getTime();
    }

    public Date getScheduledStartDate(final MessageLog userMessageLog) {
        Date result = userMessageLog.getRestored();
        if (result == null) {
            LOG.debug("Using the received date for scheduled start time [{}]", userMessageLog.getReceived());
            return userMessageLog.getReceived();
        }
        return result;
    }

    public Date getMessageExpirationDate(final MessageLog userMessageLog,
                                         final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = getScheduledStartTime(userMessageLog);
            final int timeOut = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000;
            Date result = new Date(scheduledStartTime + timeOut);
            LOG.debug("Message expiration date is [{}]", result);
            return result;
        }
        return null;
    }

    public boolean isExpired(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        int delay = domibusPropertyProvider.getIntegerProperty(MESSAGE_EXPIRATION_DELAY);
        Boolean isExpired = (getMessageExpirationDate(userMessageLog, legConfiguration).getTime() + delay) < System.currentTimeMillis();
        LOG.debug("Verify if message expired: [{}]", isExpired);
        return isExpired;
    }

    public void updateMessageLogNextAttemptDate(LegConfiguration legConfiguration, MessageLog userMessageLog) {
        Date nextAttempt = new Date();
        if (userMessageLog.getNextAttempt() != null) {
            nextAttempt = new Date(userMessageLog.getNextAttempt().getTime());
        }
        RetryStrategy.AttemptAlgorithm algorithm = legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm();
        int retryCount = legConfiguration.getReceptionAwareness().getRetryCount();
        int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
        Date newNextAttempt = algorithm.compute(nextAttempt, retryCount, retryTimeout);
        LOG.debug("Updating next attempt from [{}] to [{}]", nextAttempt, newNextAttempt);
        userMessageLog.setNextAttempt(newNextAttempt);
    }
}


