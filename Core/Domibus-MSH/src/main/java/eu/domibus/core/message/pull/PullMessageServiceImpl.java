package eu.domibus.core.message.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
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

import java.sql.Timestamp;
import java.util.Date;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_DYNAMIC_INITIATOR;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_MULTIPLE_LEGS;

@Service
public class PullMessageServiceImpl implements PullMessageService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageServiceImpl.class);

    public static final String MPC = "mpc";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private PullMessageStateService pullMessageStateService;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    protected MpcService mpcService;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    private MessageRetentionDefaultService messageRetentionService;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected UserMessageDao userMessageDao;


    @Autowired
    private ReprogrammableService reprogrammableService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePullMessageAfterRequest(final UserMessage userMessage,
                                              final String messageId,
                                              final LegConfiguration legConfiguration,
                                              final ReliabilityChecker.CheckResult state) {

        final MessagingLock lock = getLock(messageId);
        if (lock == null || MessageState.PROCESS != lock.getMessageState()) {
            LOG.warn("Message [{}] could not acquire lock when updating status, it has been handled by another process.", messageId);
            return;
        }


        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        final int sendAttempts = userMessageLog.getSendAttempts() + 1;
        LOG.debug("[PULL_REQUEST]:Message[{}]:Increasing send attempts to[{}]", messageId, sendAttempts);
        userMessageLog.setSendAttempts(sendAttempts);
        switch (state) {
            case WAITING_FOR_CALLBACK:
                waitingForCallBack(userMessage, legConfiguration, userMessageLog);
                break;
            case PULL_FAILED:
                pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
                break;
            case ABORT:
                pullMessageStateService.sendFailed(userMessageLog, userMessage.getMessageId());
                reprogrammableService.removeRescheduleInfo(lock);
                lock.setMessageState(MessageState.DEL);
                messagingLockDao.save(lock);
                break;
            default:
                throw new IllegalStateException(String.format("Status:[%s] should never occur here", state.name()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PullRequestResult updatePullMessageAfterReceipt(
            ReliabilityChecker.CheckResult reliabilityCheckSuccessful,
            ResponseHandler.ResponseStatus isOk,
            UserMessageLog userMessageLog,
            LegConfiguration legConfiguration,
            UserMessage userMessage) {
        final String messageId = userMessage.getMessageId();
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock]", messageId);

        switch (reliabilityCheckSuccessful) {
            case OK:
                switch (isOk) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(userMessage, userMessageLog);
                        LOG.debug("[PULL_RECEIPT]:Message:[{}] acknowledged.", messageId);
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(userMessage, userMessageLog);
                        LOG.debug("[PULL_RECEIPT]:Message:[{}] acknowledged with warning.", messageId);
                        break;
                    default:
                        assert false;
                }
                backendNotificationService.notifyOfSendSuccess(userMessage, userMessageLog);
                LOG.businessInfo(userMessage.isTestMessage() ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_SUCCESS : DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS,
                        userMessage.getPartyInfo().getFromParty(), userMessage.getPartyInfo().getToParty());
                messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

                userMessageLogDao.update(userMessageLog);

                return new PullRequestResult(messageId, userMessageLog);
            case PULL_FAILED:
                return pullFailedOnReceipt(userMessage, legConfiguration, userMessageLog);

        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String getPullMessageId(final String initiator, final String mpc) {
            PullMessageId pullMessageId = null;
            try {
                pullMessageId = messagingLockDao.getNextPullMessageToProcess(initiator, mpc);
            } catch (Exception ex) {
                LOG.error("Error while locking message ", ex);
            }
            if (pullMessageId != null) {
                LOG.debug("[PULL_REQUEST]:Message:[{}] retrieved", pullMessageId.getMessageId());
                final String messageId = pullMessageId.getMessageId();
                switch (pullMessageId.getState()) {
                    case EXPIRED:
                        pullMessageStateService.expirePullMessage(messageId);
                        LOG.debug("[PULL_REQUEST]:Message:[{}] is staled for reason:[{}].", pullMessageId.getMessageId(), pullMessageId.getStaledReason());
                        break;
                    case FIRST_ATTEMPT:
                        LOG.debug("[PULL_REQUEST]:Message:[{}] first pull attempt.", pullMessageId.getMessageId());
                        return messageId;
                    case RETRY:
                        LOG.debug("[PULL_REQUEST]:message:[{}] retry pull attempt.", pullMessageId.getMessageId());
                        // what mshRole?
                        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, MSHRole.SENDING);
                        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessage.getEntityId());
                        return messageId;
                }
            }
        LOG.trace("[PULL_REQUEST]:No message found.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addPullMessageLock(final UserMessage userMessage, String partyIdentifier, final String pModeKey,
                                   final UserMessageLog messageLog) {
        MessagingLock messagingLock = prepareMessagingLock(userMessage, partyIdentifier, pModeKey, messageLog);
        messagingLockDao.save(messagingLock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void addPullMessageLock(final UserMessage userMessage,
                                   final UserMessageLog messageLog) {
        final String pmodeKey; // FIXME: This does not work for signalmessages
        try {
            pmodeKey = this.pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true).getPmodeKey();
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + userMessage.getMessageId() + "]", e);
        }
        addPullMessageLock(userMessage, userMessage.getPartyInfo().getToParty(), pmodeKey, messageLog);
    }

    private MessagingLock prepareMessagingLock(UserMessage userMessage, String partyIdentifier, String pModeKey, UserMessageLog messageLog) {
        final String messageId = userMessage.getMessageId();
        final String mpc = userMessage.getMpcValue();
        LOG.trace("Saving message lock with partyID:[{}], mpc:[{}]", partyIdentifier, mpc);
        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pModeKey);
        final Date staledDate = updateRetryLoggingService.getMessageExpirationDate(messageLog, legConfiguration);

        return new MessagingLock(
                messageId,
                partyIdentifier,
                mpc,
                messageLog.getReceived(),
                staledDate,
                messageLog.getNextAttempt() == null ? new Date() : messageLog.getNextAttempt(),
                messageLog.getTimezoneOffset(),
                messageLog.getSendAttempts(),
                messageLog.getSendAttemptsMax());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deletePullMessageLock(final String messageId) {
        messagingLockDao.delete(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessagingLock getLock(final String messageId) {
        return messagingLockDao.getLock(messageId);
    }

    /**
     * This method is called when a message has been pulled successfully.
     *
     * @param legConfiguration processing information for the message
     * @param userMessageLog   the user message
     */
    protected void waitingForCallBack(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(userMessage.getMessageId());
        if (updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] expired]", userMessage.getMessageId());
            pullMessageStateService.sendFailed(userMessageLog, userMessage.getMessageId());
            reprogrammableService.removeRescheduleInfo(lock);
            lock.setMessageState(MessageState.DEL);
            messagingLockDao.save(lock);
            return;
        }
        final MessageStatus waitingForReceipt = MessageStatus.WAITING_FOR_RECEIPT;
        LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] change status to:[{}]", userMessage.getMessageId(), waitingForReceipt);
        updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        if (LOG.isDebugEnabled()) {
            if (attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration)) {
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] has been pulled [{}] times", userMessage.getMessageId(), userMessageLog.getSendAttempts());
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] In case of failure, will be available for pull at [{}]", userMessage.getMessageId(), userMessageLog.getNextAttempt());
            } else {
                LOG.debug("[WAITING_FOR_CALLBACK]:Message:[{}] has no more attempt, it has been pulled [{}] times and it will be the last try.", userMessage.getMessageId(), userMessageLog.getSendAttempts());
            }
        }
        lock.setMessageState(MessageState.WAITING);
        lock.setSendAttempts(userMessageLog.getSendAttempts());
        reprogrammableService.setRescheduleInfo(lock, userMessageLog.getNextAttempt());
        final MessageStatusEntity messageStatus = messageStatusDao.findOrCreate(waitingForReceipt);
        userMessageLog.setMessageStatus(messageStatus);
        messagingLockDao.save(lock);
        userMessageLogDao.update(userMessageLog);
        backendNotificationService.notifyOfMessageStatusChange(userMessage, userMessageLog, waitingForReceipt, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Check if the message can be sent again: there is time and attempts left
     *
     * @param userMessageLog   the message
     * @param legConfiguration processing information for the message
     * @return true if the message can be sent again
     */
    protected boolean attemptNumberLeftIsLowerOrEqualThenMaxAttempts(final UserMessageLog userMessageLog,
                                                                     final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() <= userMessageLog.getSendAttemptsMax()
                && !updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }


    protected boolean attemptNumberLeftIsStricltyLowerThenMaxAttemps(final UserMessageLog userMessageLog, final LegConfiguration legConfiguration) {
        // retries start after the first send attempt
        if (legConfiguration.getReceptionAwareness() != null && userMessageLog.getSendAttempts() < userMessageLog.getSendAttemptsMax()
                && !updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            return true;
        }
        return false;
    }

    protected void pullFailedOnRequest(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        LOG.debug("[PULL_REQUEST]:Message:[{}] failed on pull message retrieval", userMessage.getMessageId());
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(userMessage.getMessageId());
        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_REQUEST]:Message:[{}] has been pulled [{}] times", userMessage.getMessageId(), userMessageLog.getSendAttempts() + 1);
            updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
            updateRetryLoggingService.saveAndNotify(userMessage, MessageStatus.READY_TO_PULL, userMessageLog);
            LOG.debug("[pullFailedOnRequest]:Message:[{}] release lock", userMessage.getMessageId());
            LOG.debug("[PULL_REQUEST]:Message:[{}] will be available for pull at [{}]", userMessage.getMessageId(), userMessageLog.getNextAttempt());
            lock.setMessageState(MessageState.READY);
            lock.setSendAttempts(userMessageLog.getSendAttempts());
            reprogrammableService.setRescheduleInfo(lock, userMessageLog.getNextAttempt());
        } else {
            reprogrammableService.removeRescheduleInfo(lock);
            lock.setMessageState(MessageState.DEL);
            LOG.debug("[PULL_REQUEST]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessage.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog, userMessage.getMessageId());
        }
        messagingLockDao.save(lock);
    }

    protected PullRequestResult pullFailedOnReceipt(UserMessage userMessage, LegConfiguration legConfiguration, UserMessageLog
            userMessageLog) {
        LOG.debug("[PULL_RECEIPT]:Message:[{}] failed on pull message acknowledgement", userMessage.getMessageId());
        if (attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration)) {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has been pulled [{}] times", userMessage.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.reset(userMessageLog, userMessage.getMessageId());
            LOG.debug("[pullFailedOnReceipt]:Message:[{}] add lock", userMessage.getMessageId());
            LOG.debug("[PULL_RECEIPT]:Message:[{}] will be available for pull at [{}]", userMessage.getMessageId(), userMessageLog.getNextAttempt());

        } else {
            LOG.debug("[PULL_RECEIPT]:Message:[{}] has no more attempt, it has been pulled [{}] times", userMessage.getMessageId(), userMessageLog.getSendAttempts() + 1);
            pullMessageStateService.sendFailed(userMessageLog, userMessage.getMessageId());
        }
        return new PullRequestResult(userMessage.getMessageId(), userMessageLog);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final MessagingLock messagingLock) {
        messagingLockDao.delete(messagingLock);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteInNewTransaction(final String messageId) {
        final MessagingLock lock = getLock(messageId);
        if (lock != null) {
            messagingLockDao.delete(lock);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetMessageInWaitingForReceiptState(final String messageId) {
        MessagingLock lock = getLock(messageId);
        if (lock == null) {
            return;
        }
        LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] checking if can be retry, attempts[{}], max attempts[{}], expiration date[{}]", lock.getMessageId(), lock.getSendAttempts(), lock.getSendAttemptsMax(), lock.getStaled());
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(lock.getMessageId());
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        if (lock.getSendAttempts() < lock.getSendAttemptsMax() && lock.getStaled().getTime() > System.currentTimeMillis()) {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] set ready for pulling", lock.getMessageId());
            pullMessageStateService.reset(userMessageLog, messageId);
            lock.setMessageState(MessageState.READY);
            messagingLockDao.save(lock);
        } else {
            LOG.debug("[resetWaitingForReceiptPullMessages]:Message:[{}] send failed.", lock.getMessageId());
            lock.setMessageState(MessageState.DEL);
            reprogrammableService.removeRescheduleInfo(lock);
            messagingLockDao.save(lock);
            pullMessageStateService.sendFailed(userMessageLog, messageId);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireMessage(String messageId) {
        MessagingLock lock = messagingLockDao.getLock(messageId);
        if (lock != null && MessageState.ACK != lock.getMessageState()) {
            LOG.debug("[bulkExpirePullMessages]:Message:[{}] expired.", lock.getMessageId());
            UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(lock.getMessageId());
            if (userMessageLog == null) {
                throw new MessageNotFoundException(messageId);
            }
            pullMessageStateService.sendFailed(userMessageLog, messageId);
            delete(lock);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void releaseLockAfterReceipt(final PullRequestResult requestResult) {
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock]", requestResult.getMessageId());
        final MessagingLock lock = messagingLockDao.findMessagingLockForMessageId(requestResult.getMessageId());
        switch (requestResult.getMessageStatus()) {
            case READY_TO_PULL:
                lock.setMessageState(MessageState.READY);
                lock.setSendAttempts(requestResult.getSendAttempts());
                reprogrammableService.setRescheduleInfo(lock, requestResult.getNextAttempts());
                messagingLockDao.save(lock);
                break;
            case SEND_FAILURE:
                lock.setMessageState(MessageState.DEL);
                reprogrammableService.removeRescheduleInfo(lock);
                messagingLockDao.save(lock);
                break;
            case ACKNOWLEDGED:
                reprogrammableService.removeRescheduleInfo(lock);
                lock.setMessageState(MessageState.ACK);
                messagingLockDao.delete(lock);
                break;
        }
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] receive receiptResult  with status[{}] and next attempt:[{}].", requestResult.getMessageId(), requestResult.getMessageStatus(), requestResult.getNextAttempts());
        LOG.debug("[releaseLockAfterReceipt]:Message:[{}] release lock  with status[{}] and next attempt:[{}].", lock.getMessageId(), lock.getMessageState(), lock.getNextAttempt());

    }

    @Override
    public boolean allowMultipleLegsInPullProcess() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_PULL_MULTIPLE_LEGS);
    }

    @Override
    public boolean allowDynamicInitiatorInPullProcess() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_PULL_DYNAMIC_INITIATOR);
    }
}
