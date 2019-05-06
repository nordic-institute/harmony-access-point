package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.DomibusJMSException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.model.MessagingLock;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class RetryDefaultService {

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
    private JMSManager jmsManager;

    @Autowired
    private MessagingLockDao messagingLockDao;

    @Autowired
    PModeProvider pModeProvider;

    @Autowired
    UpdateRetryLoggingService updateRetryLoggingService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueMessages() {
        final List<String> messagesNotAlreadyQueued = getMessagesNotAlreadyQueued();
        for (final String messageId : messagesNotAlreadyQueued) {
            if (!failIfExpired(messageId)) {
                userMessageService.scheduleSending(messageId);
            }
        }
    }

    protected boolean failIfExpired(String messageId) {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        eu.domibus.common.model.configuration.LegConfiguration legConfiguration = null;
        final String pModeKey;

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
        } catch (EbMS3Exception exc) {
            LOG.warn("Could not find LegConfiguration for message [{}]", messageId);
            return false;
        }
        if (updateRetryLoggingService.isExpired(legConfiguration, userMessageLog)) {
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);

            if (userMessage.isUserMessageFragment()) {
                userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId());
            }
            return true;
        }
        return false;
    }

    protected List<String> getMessagesNotAlreadyQueued() {
        List<String> result = new ArrayList<>();

        final List<String> messageIdsToSend = userMessageLogDao.findRetryMessages();
        if (messageIdsToSend.isEmpty()) {
            return result;
        }
        LOG.trace("Found candidate messages to be retried [{}]", messageIdsToSend);
        final List<String> queuedMessages = getAllQueuedMessages();
        LOG.trace("Found queuedMessages [{}]", queuedMessages);

        messageIdsToSend.removeAll(queuedMessages);
        LOG.trace("After filtering the following messages will to be retried [{}]", messageIdsToSend);
        return messageIdsToSend;
    }

    protected List<String> getAllQueuedMessages() {
        final List<String> result = new ArrayList<>();
        final List<String> queuedMessages = getQueuedMessages(dispatchQueue);
        if (queuedMessages != null) {
            LOG.trace("Adding messages [{}]", queuedMessages);
            result.addAll(queuedMessages);
        }

        final List<String> queuedMessageFragments = getQueuedMessages(sendLargeMessageQueue);
        if (queuedMessageFragments != null) {
            LOG.trace("Adding message fragments [{}]", queuedMessageFragments);
            result.addAll(queuedMessageFragments);
        }

        return result;
    }


    protected List<String> getQueuedMessages(Queue dispatchQueue) {
        List<String> result = new ArrayList<>();
        try {
            final String queueName = dispatchQueue.getQueueName();
            LOG.trace("Getting queued messages from queue [{}]", queueName);
            final List<JmsMessage> jmsMessages = jmsManager.browseClusterMessages(queueName);
            if (jmsMessages == null) {
                LOG.trace("No queued messages found in queue [{}]", queueName);
                return result;
            }
            for (JmsMessage jmsMessage : jmsMessages) {
                result.add(jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID));
            }
            return result;
        } catch (JMSException e) {
            throw new DomibusJMSException(e);
        }
    }

    /**
     * Method call by job to reset waiting_for_receipt messages into ready to pull.
     */
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void bulkDeletePullMessages() {
        final List<MessagingLock> deletedLocks = messagingLockDao.findDeletedMessages();
        LOG.trace("Delete unecessary locks");
        for (MessagingLock deletedLock : deletedLocks) {
            pullMessageService.deleteInNewTransaction(deletedLock.getMessageId());
        }
    }


}
