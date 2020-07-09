package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.message.pull.MessagingLockDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service
public class RetryDefaultService implements RetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryDefaultService.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    UserMessageDefaultService userMessageService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    PModeProvider pModeProvider;

    @Autowired
    UpdateRetryLoggingService updateRetryLoggingService;

    @Override
    public void enqueueMessages() {
        final List<String> messagesNotAlreadyQueued = getMessagesNotAlreadyScheduled();

        for (final String messageId : messagesNotAlreadyQueued) {
            enqueueMessage(messageId);
        }
    }

    /**
     * Tries to enqueue a message to be retried. Sets the message id on the MDC context and cleans it afterwards
     *
     * @param messageId The message id to be enqueued for retrial
     */
    protected void enqueueMessage(String messageId) {
        try {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
            doEnqueueMessage(messageId);
        } catch (RuntimeException e) {
            LOG.warn("Could not enqueue message [{}]", messageId, e);
        } finally {
            LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ID);
        }

    }

    /**
     * Tries to enqueue a message to be retried.
     *
     * @param messageId The message id to be enqueued for retrial
     */
    protected void doEnqueueMessage(String messageId) {
        LOG.trace("Enqueueing message for retrial [{}]", messageId);

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        LegConfiguration legConfiguration = updateRetryLoggingService.failIfInvalidConfig(userMessage);
        if (legConfiguration == null) {
            LOG.warn("Message was not enqueued: invalid LegConfiguration for message [{}]", messageId);
            return;
        }

        boolean setAsExpired = updateRetryLoggingService.failIfExpired(userMessage, legConfiguration);
        if (setAsExpired) {
            LOG.debug("Message [{}] was marked as expired", messageId);
            return;
        }
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        userMessageService.scheduleSending(userMessage, userMessageLog);
    }


    protected List<String> getMessagesNotAlreadyScheduled() {
        List<String> result = new ArrayList<>();

        final List<String> messageIdsToSend = userMessageLogDao.findRetryMessages();
        if (messageIdsToSend.isEmpty()) {
            LOG.trace("No message found to be resend");
            return result;
        }
        LOG.trace("Found messages to be send [{}]", messageIdsToSend);

        return messageIdsToSend;
    }

    /**
     * Method call by job to reset waiting_for_receipt messages into ready to pull.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetWaitingForReceiptPullMessages() {
        final List<MessagingLock> messagesToReset = messagingLockDao.findWaitingForReceipt();
        for (MessagingLock messagingLock : messagesToReset) {
            pullMessageService.resetMessageInWaitingForReceiptState(messagingLock.getMessageId());
        }
    }


    /**
     * Method call by job to to expire messages that could not be delivered in the configured time range..
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkExpirePullMessages() {
        final List<MessagingLock> expiredMessages = messagingLockDao.findStaledMessages();
        LOG.trace("Delete expired pull message");
        for (MessagingLock staledMessage : expiredMessages) {
            pullMessageService.expireMessage(staledMessage.getMessageId());
        }
    }

    /**
     * Method call by job to to delete messages marked as failed.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkDeletePullMessages() {
        final List<MessagingLock> deletedLocks = messagingLockDao.findDeletedMessages();
        LOG.trace("Delete unecessary locks");
        for (MessagingLock deletedLock : deletedLocks) {
            pullMessageService.deleteInNewTransaction(deletedLock.getMessageId());
        }
    }


}
