package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Date;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UpdateRetryLoggingService {

    public static final String MESSAGE_EXPIRATION_DELAY = DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UpdateRetryLoggingService.class);

    private final BackendNotificationService backendNotificationService;

    private final UserMessageLogDao userMessageLogDao;

    private final UserMessageLogDefaultService userMessageLogService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    private final UserMessageService userMessageService;

    private final MessageAttemptService messageAttemptService;

    private final PModeProvider pModeProvider;

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    private final MessageGroupDao messageGroupDao;

    private final MessageStatusDao messageStatusDao;

    private final ReprogrammableService reprogrammableService;

    public UpdateRetryLoggingService(BackendNotificationService backendNotificationService,
                                     UserMessageLogDao userMessageLogDao,
                                     UserMessageLogDefaultService userMessageLogService,
                                     DomibusPropertyProvider domibusPropertyProvider,
                                     UserMessageRawEnvelopeDao rawEnvelopeLogDao,
                                     UserMessageService userMessageService,
                                     MessageAttemptService messageAttemptService,
                                     PModeProvider pModeProvider,
                                     MessageRetentionDefaultService messageRetentionService,
                                     MessageGroupDao messageGroupDao,
                                     MessageStatusDao messageStatusDao,
                                     ReprogrammableService reprogrammableService) {
        this.backendNotificationService = backendNotificationService;
        this.userMessageLogDao = userMessageLogDao;
        this.userMessageLogService = userMessageLogService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.rawEnvelopeLogDao = rawEnvelopeLogDao;
        this.userMessageService = userMessageService;
        this.messageAttemptService = messageAttemptService;
        this.pModeProvider = pModeProvider;
        this.messageRetentionService = messageRetentionService;
        this.messageGroupDao = messageGroupDao;
        this.messageStatusDao = messageStatusDao;
        this.reprogrammableService = reprogrammableService;
    }

    /**
     * This method is responsible for the handling of retries for a given sent message.
     * In case of failure the message will be put back in waiting_for_retry status, after a certain amount of retry/time
     * it will be marked as failed.
     *
     * @param userMessage        id of the message that needs to be retried
     * @param legConfiguration processing information for the message
     */
    @Transactional
    public void updatePushedMessageRetryLogging(final UserMessage userMessage, final LegConfiguration legConfiguration, final MessageAttempt messageAttempt) {
        updateRetryLogging(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, messageAttempt);
    }

    /**
     * Set a message as failed if it has expired
     *
     * @param userMessage The userMessage to be checked for expiration
     * @param legConfiguration
     * @return true in case the message was set as expired
     */
    @Transactional
    public boolean failIfExpired(UserMessage userMessage, final @NotNull LegConfiguration legConfiguration) {
        final String messageId = userMessage.getMessageId();
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);

        boolean expired = isExpired(legConfiguration, userMessageLog);
        if (!expired) {
            LOG.debug("Message [{}] is not expired", messageId);
            return false;
        }
        LOG.debug("Message [{}] is expired", messageId);
        setMessageFailed(userMessage, userMessageLog);
        return true;
    }

    @Transactional
    public boolean failIfInvalidConfig(UserMessage userMessage, final LegConfiguration legConfiguration) {
        final String messageId = userMessage.getMessageId();
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        if (legConfiguration == null) {
            setMessageFailed(userMessage, userMessageLog);
            return true;
        }
        return false;
    }

    protected void setMessageFailed(UserMessage userMessage, UserMessageLog userMessageLog) {
        final String messageId = userMessage.getMessageId();
        messageFailed(userMessage, userMessageLog);

        if (userMessage.isMessageFragment()) {
            MessageGroupEntity messageGroup = messageGroupDao.findByUserMessageEntityId(userMessage.getEntityId());
            userMessageService.scheduleSplitAndJoinSendFailed(messageGroup.getGroupId(), "Message fragment [" + messageId + "] has failed to be sent");
        }
    }

    public LegConfiguration getLegConfiguration(UserMessage userMessage) {
        String pModeKey;
        LegConfiguration legConfiguration = null;
        final String messageId = userMessage.getMessageId();
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (EbMS3Exception e) {
            LOG.debug("PMode key not found for message: [{}]", messageId, e);
            return null;
        }
        LOG.debug("PMode key found: [{}]", pModeKey);

        try {
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
        } catch (ConfigurationException e) {
            LOG.debug("LegConfiguration not found for message: [{}]", messageId, e);
        }
        return legConfiguration;
    }


    protected void updateRetryLogging(final UserMessage userMessage, final LegConfiguration legConfiguration, MessageStatus messageStatus, final MessageAttempt messageAttempt) {
        LOG.debug("Updating retry for message");
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(userMessage.getMessageId(), MSHRole.SENDING);
        userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
        LOG.debug("Updating sendAttempts to [{}]", userMessageLog.getSendAttempts());
        userMessageLog.setNextAttempt(getScheduledStartDate(userMessageLog)); // this is needed for the first computation of "next attempt" if receiver is down

        userMessageLog.setScheduled(false);
        LOG.debug("Scheduled flag for message [{}] has been reset to false", userMessage);

        userMessageLogDao.update(userMessageLog);
        if (hasAttemptsLeft(userMessageLog, legConfiguration) && !userMessage.isTestMessage()) {
            updateNextAttemptAndNotify(userMessage, legConfiguration, messageStatus, userMessageLog);
        } else { // max retries reached, mark message as ultimately failed (the message may be pushed back to the send queue by an administrator but this send completely failed)
            setMessageFailed(userMessage, userMessageLog);
        }

        if (messageAttempt != null) {
            messageAttempt.setUserMessageEntityId(userMessage.getEntityId());
            messageAttemptService.createAndUpdateEndDate(messageAttempt);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void messageFailedInANewTransaction(UserMessage userMessage, UserMessageLog userMessageLog, final MessageAttempt messageAttempt) {
        LOG.debug("Marking message [{}] as failed in a new transaction", userMessage.getMessageId());

        messageFailed(userMessage, userMessageLog);
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessage.getEntityId());
    }

    public void messageFailed(UserMessage userMessage, UserMessageLog userMessageLog) {
        LOG.debug("Marking message [{}] as failed", userMessage.getMessageId());

        NotificationStatusEntity notificationStatus = userMessageLog.getNotificationStatus();
        boolean isTestMessage = userMessage.isTestMessage();

        LOG.businessError(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_FAILURE : DomibusMessageCode.BUS_MESSAGE_SEND_FAILURE,
                userMessage.getPartyInfo().getFromParty(), userMessage.getPartyInfo().getToParty());
        if (NotificationStatus.REQUIRED.equals(notificationStatus.getStatus()) && !isTestMessage) {
            LOG.info("Notifying backend for message failure");
            backendNotificationService.notifyOfSendFailure(userMessage, userMessageLog);
        }

        userMessageLogService.setMessageAsSendFailure(userMessage, userMessageLog);
        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);
    }

    public void setSourceMessageAsFailed(UserMessage userMessage) {
        final String messageId = userMessage.getMessageId();
        LOG.debug("Setting the SourceMessage [{}] as failed", messageId);

        final UserMessageLog messageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        if (messageLog == null) {
            LOG.error("UserMessageLogEntity not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }
        messageFailed(userMessage, messageLog);
    }


    @Transactional
    public void updateWaitingReceiptMessageRetryLogging(final UserMessage userMessage, final LegConfiguration legConfiguration) {
        LOG.debug("Updating waiting receipt retry for message");
        updateRetryLogging(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RECEIPT, null);
    }

    protected void updateNextAttemptAndNotify(UserMessage userMessage, LegConfiguration legConfiguration, MessageStatus messageStatus, UserMessageLog userMessageLog) {
        updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        saveAndNotify(userMessage, messageStatus, userMessageLog);
    }

    public void saveAndNotify(UserMessage userMessage, MessageStatus messageStatus, UserMessageLog userMessageLog) {
        backendNotificationService.notifyOfMessageStatusChange(userMessage, userMessageLog, messageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(messageStatusDao.findOrCreate(messageStatus));
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
    public boolean hasAttemptsLeft(final UserMessageLog userMessageLog, final LegConfiguration legConfiguration) {
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
    public Long getScheduledStartTime(final UserMessageLog userMessageLog) {
        return getScheduledStartDate(userMessageLog).getTime();
    }

    public Date getScheduledStartDate(final UserMessageLog userMessageLog) {
        Date result = userMessageLog.getRestored();
        if (result == null) {
            LOG.debug("Using the received date for scheduled start time [{}]", userMessageLog.getReceived());
            return userMessageLog.getReceived();
        }
        return result;
    }

    public Date getMessageExpirationDate(final UserMessageLog userMessageLog,
                                         final LegConfiguration legConfiguration) {
        if (legConfiguration.getReceptionAwareness() != null) {
            final Long scheduledStartTime = getScheduledStartTime(userMessageLog);
            final long timeOut = legConfiguration.getReceptionAwareness().getRetryTimeout() * 60000L;
            Date result = new Date(scheduledStartTime + timeOut);
            LOG.debug("Message expiration date is [{}]", result);
            return result;
        }
        return null;
    }

    public boolean isExpired(LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        long delay = domibusPropertyProvider.getLongProperty(MESSAGE_EXPIRATION_DELAY);
        Boolean isExpired = (getMessageExpirationDate(userMessageLog, legConfiguration).getTime() + delay) < System.currentTimeMillis();
        LOG.debug("Verify if message expired: [{}]", isExpired);
        return isExpired;
    }

    public void updateMessageLogNextAttemptDate(LegConfiguration legConfiguration, UserMessageLog userMessageLog) {
        Date nextAttempt = new Date();
        if (userMessageLog.getNextAttempt() != null) {
            nextAttempt = new Date(userMessageLog.getNextAttempt().getTime());
        }
        RetryStrategy.AttemptAlgorithm algorithm = legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm();
        int retryCount = legConfiguration.getReceptionAwareness().getRetryCount();
        int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
        long delayInMillis = domibusPropertyProvider.getLongProperty(MESSAGE_EXPIRATION_DELAY);
        int crtInterval = 0;
        if (legConfiguration.getReceptionAwareness().getStrategy() == RetryStrategy.PROGRESSIVE) {
            crtInterval = legConfiguration.getReceptionAwareness().getRetryIntervals().get(userMessageLog.getSendAttempts()-1);
        }

        Date newNextAttempt = algorithm.compute(nextAttempt, retryCount, retryTimeout, crtInterval, delayInMillis);

        LOG.debug("Updating next attempt from [{}] to [{}]", nextAttempt, newNextAttempt);
        reprogrammableService.setRescheduleInfo(userMessageLog, newNextAttempt);
    }
}


