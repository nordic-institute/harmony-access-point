package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.jms.DelayedDispatchMessageCreator;
import eu.domibus.core.jms.DispatchMessageCreator;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.core.message.attempt.MessageAttemptDao;
import eu.domibus.core.message.converter.MessageConverterService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.message.splitandjoin.MessageGroupEntity;
import eu.domibus.core.message.splitandjoin.SplitAndJoinException;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageDefaultService implements UserMessageService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultService.class);
    static final String MESSAGE = "Message [";
    static final String DOES_NOT_EXIST = "] does not exist";

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

    @Autowired
    @Qualifier("sendLargeMessageQueue")
    private Queue sendLargeMessageQueue;

    @Autowired
    @Qualifier("splitAndJoinQueue")
    private Queue splitAndJoinQueue;

    @Autowired
    @Qualifier("sendPullReceiptQueue")
    private Queue sendPullReceiptQueue;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private MessageInfoDao messageInfoDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private MessageAttemptDao messageAttemptDao;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private UIMessageDao uiMessageDao;

    @Autowired
    private MessageAcknowledgementDao messageAcknowledgementDao;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    protected UserMessagePriorityService userMessagePriorityService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    PModeService pModeService;

    @Autowired
    PModeServiceHelper pModeServiceHelper;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    protected DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    MessageConverterService messageConverterService;

    @Autowired
    private AuditService auditService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DateUtil dateUtil;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    public void createMessageFragments(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles) {
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(sourceMessage.getMessageInfo().getMessageId());
        for (int index = 0; index < fragmentFiles.size(); index++) {
            try {
                final String fragmentFile = fragmentFiles.get(index);
                createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragmentFile, index + 1);
            } catch (MessagingProcessingException e) {
                throw new SplitAndJoinException("Could not create Messaging for fragment " + index, e);
            }
        }
    }

    protected void createMessagingForFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, String backendName, String fragmentFile, int index) throws MessagingProcessingException {
        final UserMessage userMessageFragment = userMessageFactory.createUserMessageFragment(userMessage, messageGroupEntity, Long.valueOf(index), fragmentFile);
        databaseMessageHandler.submitMessageFragment(userMessageFragment, backendName);
    }

    @Override
    public String getFinalRecipient(String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return null;
        }
        return userMessageServiceHelper.getFinalRecipient(userMessage);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient) {
        LOG.debug("Provided finalRecipient is [{}]", finalRecipient);
        return userMessageLogDao.findFailedMessages(finalRecipient);
    }

    @Override
    public Long getFailedMessageElapsedTime(String messageId) {
        final UserMessageLog userMessageLog = getFailedMessage(messageId);
        final Date failedDate = userMessageLog.getFailed();
        if (failedDate == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not compute failed elapsed time for message [" + messageId + "]: failed date is empty");
        }
        return System.currentTimeMillis() - failedDate.getTime();

    }

    @Transactional
    @Override
    public void restoreFailedMessage(String messageId) {
        LOG.info("Restoring message [{}]", messageId);
        final UserMessageLog userMessageLog = getFailedMessage(messageId);

        if (MessageStatus.DELETED == userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "]. Message status is [" + MessageStatus.DELETED + "]");
        }

        final MessageStatus newMessageStatus = messageExchangeService.retrieveMessageRestoreStatus(messageId);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, newMessageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(newMessageStatus);
        final Date currentDate = new Date();
        userMessageLog.setRestored(currentDate);
        userMessageLog.setFailed(null);
        userMessageLog.setNextAttempt(currentDate);

        Integer newMaxAttempts = computeNewMaxAttempts(userMessageLog, messageId);
        LOG.debug("Increasing the max attempts for message [{}] from [{}] to [{}]", messageId, userMessageLog.getSendAttemptsMax(), newMaxAttempts);
        userMessageLog.setSendAttemptsMax(newMaxAttempts);

        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (MessageStatus.READY_TO_PULL != newMessageStatus) {
            scheduleSending(userMessage, userMessageLog);
        } else {
            try {
                MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
                Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
                LOG.debug("[restoreFailedMessage]:Message:[{}] add lock", userMessageLog.getMessageId());
                pullMessageService.addPullMessageLock(userMessage, userMessageLog);
            } catch (EbMS3Exception ebms3Ex) {
                LOG.error("Error restoring user message to ready to pull[" + userMessage.getMessageInfo().getMessageId() + "]", ebms3Ex);
            }
        }
    }

    @Override
    public void sendEnqueuedMessage(String messageId) {
        LOG.info("Sending enqueued message [{}]", messageId);

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + DOES_NOT_EXIST);
        }
        if (MessageStatus.SEND_ENQUEUED != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + "] status is not [" + MessageStatus.SEND_ENQUEUED + "]");
        }

        int resendButtonReceivedMinutes = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES);
        Date receivedDateDelta = DateUtils.addMinutes(userMessageLog.getReceived(), resendButtonReceivedMinutes);
        Date currentDate = new Date();
        if (receivedDateDelta.after(currentDate)) {
            throw new UserMessageException("You have to wait " + dateUtil.getDiffMinutesBetweenDates(receivedDateDelta, currentDate) + " minutes before resending the message [" + messageId + "]");
        }
        if (userMessageLog.getNextAttempt() != null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + "] was already scheduled");
        }

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        userMessageLog.setNextAttempt(new Date());
        userMessageLogDao.update(userMessageLog);
        scheduleSending(userMessage, userMessageLog);
    }

    @Transactional
    @Override
    public void resendFailedOrSendEnqueuedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + DOES_NOT_EXIST);
        }
        if (MessageStatus.SEND_ENQUEUED == userMessageLog.getMessageStatus()) {
            sendEnqueuedMessage(messageId);
        } else {
            restoreFailedMessage(messageId);
        }

        auditService.addMessageResentAudit(messageId);
    }

    protected Integer getMaxAttemptsConfiguration(final String messageId) {
        final LegConfiguration legConfiguration = pModeService.getLegConfiguration(messageId);
        Integer result = 1;
        if (legConfiguration == null) {
            LOG.warn("Could not get the leg configuration for message [{}]. Using the default maxAttempts configuration [{}]", messageId, result);
        } else {
            result = pModeServiceHelper.getMaxAttempts(legConfiguration);
        }
        return result;
    }

    protected Integer computeNewMaxAttempts(final UserMessageLog userMessageLog, final String messageId) {
        Integer maxAttemptsConfiguration = getMaxAttemptsConfiguration(messageId);
        // always increase maxAttempts (even when not reached by sendAttempts)
        return userMessageLog.getSendAttemptsMax() + maxAttemptsConfiguration + 1; // max retries plus initial reattempt
    }

    protected Integer getUserMessagePriority(UserMessage userMessage) {
        String service = userMessageServiceHelper.getService(userMessage);
        String action = userMessageServiceHelper.getAction(userMessage);
        Integer priority = userMessagePriorityService.getPriority(service, action);
        LOG.debug("Determined priority [{}]", priority);

        return priority;
    }


    public void scheduleSending(UserMessage userMessage, UserMessageLog userMessageLog) {
        scheduleSending(userMessage, userMessageLog, new DispatchMessageCreator(userMessageLog.getMessageId()).createMessage());
    }

    @Override
    public void scheduleSending(String messageId, Long delay) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        scheduleSending(userMessage, userMessageLog, new DelayedDispatchMessageCreator(messageId, delay).createMessage());
    }

    public void scheduleSending(UserMessageLog userMessageLog, int retryCount) {
        String messageId = userMessageLog.getMessageId();
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        scheduleSending(userMessage, messageId, userMessageLog, new DispatchMessageCreator(messageId).createMessage(retryCount));
    }

    /**
     * It sends the JMS message to either {@code sendMessageQueue} or {@code sendLargeMessageQueue}
     *
     * @param userMessageLog
     * @param jmsMessage
     */
    protected void scheduleSending(final UserMessage userMessage, final UserMessageLog userMessageLog, JmsMessage jmsMessage) {
        scheduleSending(userMessage, userMessageLog.getMessageId(), userMessageLog, jmsMessage);
    }

    @Timer(clazz = DatabaseMessageHandler.class,value = "scheduleSending")
    @Counter(clazz = DatabaseMessageHandler.class,value = "scheduleSending")
    protected void scheduleSending(final UserMessage userMessage, final String messageId, UserMessageLog userMessageLog, JmsMessage jmsMessage) {
        if (userMessageLog.isSplitAndJoin()) {
            LOG.debug("Sending message to sendLargeMessageQueue");
            jmsManager.sendMessageToQueue(jmsMessage, sendLargeMessageQueue);
        } else {
            Integer priority = getUserMessagePriority(userMessage);
            jmsMessage.setPriority(priority);
            LOG.debug("Sending message to sendMessageQueue");
            jmsManager.sendMessageToQueue(jmsMessage, sendMessageQueue);
        }

        LOG.debug("Updating UserMessageLog for message id [{}]", messageId);
        userMessageLog.setScheduled(true);
        userMessageLogDao.update(userMessageLog);
    }

    @Override
    public void scheduleSourceMessageSending(String messageId) {
        LOG.debug("Sending message to sendLargeMessageQueue");
        final JmsMessage jmsMessage = new DispatchMessageCreator(messageId).createMessage();
        jmsManager.sendMessageToQueue(jmsMessage, sendLargeMessageQueue);
    }

    @Override
    public void scheduleSplitAndJoinSendFailed(String groupId, String errorDetail) {
        LOG.debug("Scheduling marking the group [{}] as failed", groupId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SPLIT_AND_JOIN_SEND_FAILED)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_EBMS3_ERROR_DETAIL, errorDetail)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSetUserMessageFragmentAsFailed(String messageId) {
        LOG.debug("Scheduling marking the UserMessage fragment [{}] as failed", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SET_MESSAGE_FRAGMENT_AS_FAILED)
                .property(UserMessageService.MSG_USER_MESSAGE_ID, messageId)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageRejoinFile(String groupId, String backendName) {
        LOG.debug("Scheduling the SourceMessage file rejoining for group [{}]", groupId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN_FILE)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_BACKEND_NAME, backendName)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageRejoin(String groupId, String file, String backendName) {
        LOG.debug("Scheduling the SourceMessage rejoining for group [{}] from file [{}]", groupId, file);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_SOURCE_MESSAGE_FILE, file)
                .property(UserMessageService.MSG_BACKEND_NAME, backendName)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSourceMessageReceipt(String messageId, String pmodeKey) {
        LOG.debug("Scheduling the SourceMessage receipt for message [{}]", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SOURCE_MESSAGE_RECEIPT)
                .property(UserMessageService.MSG_SOURCE_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSendingSignalError(String messageId, String ebMS3ErrorCode, String errorDetail, String pmodeKey) {
        LOG.debug("Scheduling sending the Signal error for message [{}]", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SEND_SIGNAL_ERROR)
                .property(UserMessageService.MSG_USER_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .property(UserMessageService.MSG_EBMS3_ERROR_CODE, ebMS3ErrorCode)
                .property(UserMessageService.MSG_EBMS3_ERROR_DETAIL, errorDetail)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSplitAndJoinReceiveFailed(String groupId, String sourceMessageId, String errorCode, String errorDetail) {
        LOG.debug("Scheduling marking the SplitAndJoin receive failed for group [{}]", groupId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SPLIT_AND_JOIN_RECEIVE_FAILED)
                .property(UserMessageService.MSG_GROUP_ID, groupId)
                .property(UserMessageService.MSG_SOURCE_MESSAGE_ID, sourceMessageId)
                .property(UserMessageService.MSG_EBMS3_ERROR_CODE, errorCode)
                .property(UserMessageService.MSG_EBMS3_ERROR_DETAIL, errorDetail)
                .build();
        jmsManager.sendMessageToQueue(jmsMessage, splitAndJoinQueue);
    }

    @Override
    public void scheduleSendingPullReceipt(final String messageId, final String pmodeKey) {
        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(PULL_RECEIPT_REF_TO_MESSAGE_ID, messageId)
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        LOG.debug("Sending message to sendPullReceiptQueue");
        jmsManager.sendMessageToQueue(jmsMessage, sendPullReceiptQueue);
    }

    @Override
    public void scheduleSendingPullReceipt(final String messageId, final String pmodeKey, final int retryCount) {
        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(PULL_RECEIPT_REF_TO_MESSAGE_ID, messageId)
                .property(MessageConstants.RETRY_COUNT, String.valueOf(retryCount))
                .property(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        LOG.debug("Sending message to sendPullReceiptQueue");
        jmsManager.sendMessageToQueue(jmsMessage, sendPullReceiptQueue);
    }

    @Override
    public eu.domibus.api.usermessage.domain.UserMessage getMessage(String messageId) {
        final UserMessage userMessageByMessageId = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessageByMessageId == null) {
            return null;
        }
        return domainConverter.convert(userMessageByMessageId, eu.domibus.api.usermessage.domain.UserMessage.class);
    }

    @Transactional
    @Override
    public List<String> restoreFailedMessagesDuringPeriod(Date start, Date end, String finalRecipient) {
        final List<String> failedMessages = userMessageLogDao.findFailedMessages(finalRecipient, start, end);
        if (failedMessages == null) {
            return null;
        }
        LOG.debug("Found failed messages [{}] using start date [{}], end date [{}] and final recipient", failedMessages, start, end, finalRecipient);

        final List<String> restoredMessages = new ArrayList<>();
        for (String messageId : failedMessages) {
            try {
                restoreFailedMessage(messageId);
                restoredMessages.add(messageId);
            } catch (Exception e) {
                LOG.error("Failed to restore message [" + messageId + "]", e);
            }
        }

        LOG.debug("Restored messages [{}] using start date [{}], end date [{}] and final recipient", restoredMessages, start, end, finalRecipient);

        return restoredMessages;
    }

    @Transactional
    @Override
    public void deleteFailedMessage(String messageId) {
        getFailedMessage(messageId);
        deleteMessage(messageId);
    }

    protected UserMessageLog getFailedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + DOES_NOT_EXIST);
        }
        if (MessageStatus.SEND_FAILURE != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + "] status is not [" + MessageStatus.SEND_FAILURE + "]");
        }
        return userMessageLog;
    }

    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void deleteMessage(String messageId) {
        LOG.debug("Deleting message [{}]", messageId);

        //add messageId to MDC map
        if (isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId);

        backendNotificationService.notifyMessageDeleted(messageId, userMessageLog);

        Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        UserMessage userMessage = messaging.getUserMessage();
        messagingDao.clearPayloadData(userMessage);

        if (MessageStatus.ACKNOWLEDGED != userMessageLog.getMessageStatus() &&
                MessageStatus.ACKNOWLEDGED_WITH_WARNING != userMessageLog.getMessageStatus()) {
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
        }

        userMessageLogService.setSignalMessageAsDeleted(messaging.getSignalMessage());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMessages(List<String> userMessageIds) {
        LOG.debug("Deleting messages [{}]", userMessageIds);
        List<String> signalMessageIds = messageInfoDao.findSignalMessageIds(userMessageIds);
        List<Long> receiptIds = signalMessageDao.findReceiptIdsByMessageIds(signalMessageIds);

        int deleteResult = messageInfoDao.deleteMessages(userMessageIds);
        LOG.debug("Deleted [{}] messageInfo for userMessage.", deleteResult);
        deleteResult = messageInfoDao.deleteMessages(signalMessageIds);
        LOG.debug("Deleted [{}] messageInfo for signalMessage.", deleteResult);
        deleteResult = signalMessageDao.deleteReceipts(receiptIds);
        LOG.debug("Deleted [{}] receipts.", deleteResult);
        deleteResult = userMessageLogDao.deleteMessageLogs(userMessageIds);
        LOG.debug("Deleted [{}] userMessageLogs.", deleteResult);
        deleteResult = signalMessageLogDao.deleteMessageLogs(signalMessageIds);
        LOG.debug("Deleted [{}] signalMessageLogs.", deleteResult);
        deleteResult = messageAttemptDao.deleteAttemptsByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] messageSendAttempts.", deleteResult);
        deleteResult = errorLogDao.deleteErrorLogsByMessageIdInError(userMessageIds);
        LOG.debug("Deleted [{}] deleteErrorLogsByMessageIdInError.", deleteResult);
        deleteResult = uiMessageDao.deleteUIMessagesByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] deleteUIMessagesByMessageIds for userMessages.", deleteResult);
        deleteResult = uiMessageDao.deleteUIMessagesByMessageIds(signalMessageIds);
        LOG.debug("Deleted [{}] deleteUIMessagesByMessageIds for signalMessages.", deleteResult);
        deleteResult = messageAcknowledgementDao.deleteMessageAcknowledgementsByMessageIds(userMessageIds);
        LOG.debug("Deleted [{}] deleteMessageAcknowledgementsByMessageIds.", deleteResult);
    }

    @Override
    public byte[] getMessageAsBytes(String messageId) throws MessageNotFoundException {
        UserMessage userMessage = getUserMessageById(messageId);
        auditService.addMessageDownloadedAudit(messageId);
        return messageToBytes(userMessage);
    }

    @Override
    public byte[] getMessageWithAttachmentsAsZip(String messageId) throws MessageNotFoundException, IOException {
        Map<String, InputStream> message = getMessageContentWithAttachments(messageId);
        return zipFiles(message);
    }

    protected Map<String, InputStream> getMessageContentWithAttachments(String messageId) throws MessageNotFoundException {

        UserMessage userMessage = getUserMessageById(messageId);

        Map<String, InputStream> result = new HashMap<>();
        InputStream messageStream = messageToStream(userMessage);
        result.put("message.xml", messageStream);

        if (userMessage.getPayloadInfo() == null || CollectionUtils.isEmpty(userMessage.getPayloadInfo().getPartInfo())) {
            LOG.info("No payload info found for message with id [{}]", messageId);
            return result;
        }

        final List<PartInfo> partInfos = userMessage.getPayloadInfo().getPartInfo();
        for (PartInfo pInfo : partInfos) {
            if (pInfo.getPayloadDatahandler() == null) {
                try {
                    messageStream.close();
                } catch (IOException e) {
                    LOG.debug("Error encountered while trying to close the message input stream.", e);
                }
                throw new MessageNotFoundException("Could not find attachment for [" + pInfo.getHref() + "]");
            }
            try {
                result.put(getPayloadName(pInfo), pInfo.getPayloadDatahandler().getInputStream());
            } catch (IOException e) {
                throw new MessagingException("Error getting input stream for attachment [" + pInfo.getHref() + "]", e);
            }
        }

        auditService.addMessageDownloadedAudit(messageId);

        return result;
    }

    @Override
    public boolean shouldDeletePayloadOnSendSuccess() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
    }

    protected UserMessage getUserMessageById(String messageId) throws MessageNotFoundException {
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException("Could not find message metadata for id " + messageId);
        }

        return userMessage;
    }

    protected InputStream messageToStream(UserMessage userMessage) {
        return new ByteArrayInputStream(messageToBytes(userMessage));
    }

    protected byte[] messageToBytes(UserMessage userMessage) {
        Messaging message = new Messaging();
        message.setUserMessage(userMessage);

        return messageConverterService.getAsByteArray(message);
    }

    protected String getPayloadName(PartInfo info) {
        if (StringUtils.isEmpty(info.getHref())) {
            return "bodyload";
        }
        if (!info.getHref().contains("cid:")) {
            LOG.warn("PayloadId does not contain \"cid:\" prefix [{}]", info.getHref());
            return info.getHref();
        }

        return info.getHref().replace("cid:", "");
    }

    private byte[] zipFiles(Map<String, InputStream> message) throws IOException {
        //creating byteArray stream, make it bufferable and passing this buffer to ZipOutputStream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
             ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {

            for (Map.Entry<String, InputStream> entry : message.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));

                IOUtils.copy(entry.getValue(), zipOutputStream);

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
            zipOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }
}
