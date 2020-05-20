package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.message.pull.MessagingLockDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class RetryDefaultService implements RetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetryDefaultService.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue dispatchQueue;

    @Autowired
    @Qualifier("sendLargeMessageQueue")
    private Queue sendLargeMessageQueue;

    @Autowired
    UserMessageService userMessageService;

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
            try {
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
                final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
                if (!failIfExpired(userMessage)) {
                    userMessageService.scheduleSending(messageId, userMessage.isSplitAndJoin());
                }
            } finally {
                LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ID);
            }
        }
    }

    protected boolean failIfExpired(UserMessage userMessage) {

        final String messageId = userMessage.getMessageInfo().getMessageId();
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        eu.domibus.common.model.configuration.LegConfiguration legConfiguration;
        final String pModeKey;

        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : {}", pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
        } catch (EbMS3Exception exc) {
            LOG.warn("Could not find LegConfiguration for message [{}]", messageId);
            return false;
        }
        if (updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);

            if (userMessage.isUserMessageFragment()) {
                userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId(), String.format("Message fragment [%s] has failed to be sent", messageId));

            }
            return true;
        }
        return false;
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
