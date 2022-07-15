package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.jms.DispatchMessageCreator;
import eu.domibus.core.message.acknowledge.MessageAcknowledgementDao;
import eu.domibus.core.message.attempt.MessageAttemptDao;
import eu.domibus.core.message.converter.MessageConverterService;
import eu.domibus.core.message.dictionary.MessagePropertyDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.SignalMessageRawEnvelopeDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageDefaultService implements UserMessageService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultService.class);
    static final String MESSAGE = "Message [";
    public static final int BATCH_SIZE = 100;
    static final String PAYLOAD_NAME = "PayloadName";
    static final String MIME_TYPE = "MimeType";

    @Autowired
    @Qualifier(InternalJMSConstants.SEND_MESSAGE_QUEUE)
    private Queue sendMessageQueue;

    @Autowired
    @Qualifier(InternalJMSConstants.SEND_LARGE_MESSAGE_QUEUE)
    private Queue sendLargeMessageQueue;

    @Autowired
    @Qualifier(InternalJMSConstants.SPLIT_AND_JOIN_QUEUE)
    private Queue splitAndJoinQueue;

    @Autowired
    @Qualifier(InternalJMSConstants.SEND_PULL_RECEIPT_QUEUE)
    private Queue sendPullReceiptQueue;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private MessageAttemptDao messageAttemptDao;

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
    private MessageCoreMapper messageCoreMapper;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    MessageConverterService messageConverterService;

    @Autowired
    private AuditService auditService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    NonRepudiationService nonRepudiationService;

    @Autowired
    protected PartInfoService partInfoService;

    @Autowired
    protected MessagePropertyDao messagePropertyDao;

    @Autowired
    private ReprogrammableService reprogrammableService;

    @Autowired
    SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    @Autowired
    UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;

    @Autowired
    ReceiptDao receiptDao;

    @Autowired
    private MessagesLogService messagesLogService;

    @Autowired
    private FileServiceUtil fileServiceUtil;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    @Override
    public String getFinalRecipient(String messageId, MSHRole mshRole) {
        // what mshRole? receive it as param or have it in MDC context?
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return null;
        }
        return userMessageServiceHelper.getFinalRecipient(userMessage);
    }

    @Override
    public String getOriginalSender(String messageId, MSHRole mshRole) {
        // what mshRole? receive it as param or have it in MDC context?
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Message [{}] does not exist", messageId);
            return null;
        }
        return userMessageServiceHelper.getOriginalSender(userMessage);
    }

    @Override
    public List<String> getFailedMessages(String finalRecipient, String originalUser) {
        LOG.debug("Provided finalRecipient is [{}]", finalRecipient);
        return userMessageLogDao.findFailedMessages(finalRecipient, originalUser);
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
    public void sendEnqueuedMessage(String messageId) {
        LOG.info("Sending enqueued message [{}]", messageId);

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId, MSHRole.SENDING);
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

        final UserMessage userMessage = userMessageDao.findByEntityId(userMessageLog.getEntityId());

        reprogrammableService.setRescheduleInfo(userMessageLog, new Date());
        userMessageLogDao.update(userMessageLog);
        scheduleSending(userMessage, userMessageLog);
    }

    protected Integer getUserMessagePriority(UserMessage userMessage) {
        String service = userMessageServiceHelper.getService(userMessage);
        String action = userMessageServiceHelper.getAction(userMessage);
        Integer priority = userMessagePriorityService.getPriority(service, action);
        LOG.debug("Determined priority [{}]", priority);

        return priority;
    }


    public void scheduleSending(UserMessage userMessage, UserMessageLog userMessageLog) {
        scheduleSending(userMessage, userMessageLog, getDispatchMessageCreator(userMessage.getMessageId(), userMessage.getEntityId()).createMessage());
    }

    protected DispatchMessageCreator getDispatchMessageCreator(String userMessageId, long userMessageEntityId) {
        return new DispatchMessageCreator(userMessageId, userMessageEntityId);
    }

    @Transactional
    public void scheduleSending(UserMessageLog userMessageLog, int retryCount) {
        UserMessage userMessage = userMessageDao.read(userMessageLog.getEntityId());
        scheduleSending(userMessage, userMessage.getMessageId(), userMessageLog, getDispatchMessageCreator(userMessage.getMessageId(), userMessage.getEntityId()).createMessage(retryCount));
    }

    /**
     * It sends the JMS message to either {@code sendMessageQueue} or {@code sendLargeMessageQueue}
     */
    protected void scheduleSending(final UserMessage userMessage, final UserMessageLog userMessageLog, JmsMessage jmsMessage) {
        scheduleSending(userMessage, userMessage.getMessageId(), userMessageLog, jmsMessage);
    }

    @Timer(clazz = UserMessageDefaultService.class, value = "scheduleSending")
    @Counter(clazz = UserMessageDefaultService.class, value = "scheduleSending")
    protected void scheduleSending(final UserMessage userMessage, final String messageId, UserMessageLog userMessageLog, JmsMessage jmsMessage) {
        if (userMessage.isSplitAndJoin()) {
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
    public void scheduleSourceMessageSending(String messageId, Long messageEntityId) {
        LOG.debug("Sending message to sendLargeMessageQueue");
        final JmsMessage jmsMessage = getDispatchMessageCreator(messageId, messageEntityId).createMessage();
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
    public void scheduleSetUserMessageFragmentAsFailed(String messageId, MSHRole role) {
        LOG.debug("Scheduling marking the UserMessage fragment [{}] as failed", messageId);

        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(UserMessageService.MSG_TYPE, UserMessageService.COMMAND_SET_MESSAGE_FRAGMENT_AS_FAILED)
                .property(UserMessageService.MSG_USER_MESSAGE_ID, messageId)
                .property(UserMessageService.MSG_MSH_ROLE, role.name())
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
                .property(UserMessageService.MSG_MSH_ROLE, MSHRole.SENDING.name())
                .property(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
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
                .property(UserMessageService.MSG_MSH_ROLE, MSHRole.SENDING.name())
                .property(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
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
                .property(UserMessageService.MSG_MSH_ROLE, MSHRole.RECEIVING.name())
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
                .property(UserMessageService.MSG_MSH_ROLE, MSHRole.SENDING.name())
                .property(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        LOG.debug("Sending message to sendPullReceiptQueue");
        jmsManager.sendMessageToQueue(jmsMessage, sendPullReceiptQueue);
    }

    @Override
    public void scheduleSendingPullReceipt(final String messageId, final String pmodeKey, final int retryCount) {
        final JmsMessage jmsMessage = JMSMessageBuilder
                .create()
                .property(PULL_RECEIPT_REF_TO_MESSAGE_ID, messageId)
                .property(UserMessageService.MSG_MSH_ROLE, MSHRole.SENDING.name())
                .property(MessageConstants.RETRY_COUNT, String.valueOf(retryCount))
                .property(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey)
                .build();
        LOG.debug("Sending message to sendPullReceiptQueue");
        jmsManager.sendMessageToQueue(jmsMessage, sendPullReceiptQueue);
    }

    @Override
    public eu.domibus.api.usermessage.domain.UserMessage getMessage(String messageId, MSHRole mshRole) {
        final UserMessage userMessageByMessageId = getMessageEntity(messageId, mshRole);
        return messageCoreMapper.userMessageToUserMessageApi(userMessageByMessageId);
    }

    @Override
    public UserMessage getMessageEntity(String messageId, MSHRole role) {
        return userMessageDao.findByMessageId(messageId, role);
    }

    @Override
    public UserMessage getMessageEntity(Long messageEntityId) {
        return userMessageDao.findByEntityId(messageEntityId);
    }

    @Transactional
    @Override
    public void deleteFailedMessage(String messageId) {
        getFailedMessage(messageId);
        deleteMessage(messageId, MSHRole.SENDING);
    }

    @Transactional
    @Override
    public void deleteMessageNotInFinalStatus(String messageId, MSHRole mshRole) {
        UserMessageLog mes = getMessageNotInFinalStatus(messageId, mshRole);
        deleteMessage(messageId, mes.getMshRole().getRole());
    }

    protected UserMessageLog getFailedMessage(String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING); // is it always???
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId, MSHRole.SENDING);
        }
        if (MessageStatus.SEND_FAILURE != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, MESSAGE + messageId + "] status is not [" + MessageStatus.SEND_FAILURE + "]");
        }
        return userMessageLog;
    }

    protected UserMessageLog getMessageNotInFinalStatus(String messageId, MSHRole mshRole) {
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, mshRole);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }

        if (userMessageLog.getDeleted() != null) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_007, MESSAGE + messageId + "] in state [" + userMessageLog.getMessageStatus().name() + "] is already deleted. Delete time: [" + userMessageLog.getDeleted() + "]", null);
        }

        if (MessageStatus.getSuccessfulStates().contains(userMessageLog.getMessageStatus())) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_007, MESSAGE + messageId + "] is in final state [" + userMessageLog.getMessageStatus().name() + "]", null);
        }

        return userMessageLog;
    }


    @Transactional
    @Override
    public List<String> deleteMessagesDuringPeriod(Long start, Long end, String finalRecipient) {
        final List<UserMessageLogDto> messagesToDelete = userMessageLogDao.findMessagesToDelete(finalRecipient, start, end);
        if (CollectionUtils.isEmpty(messagesToDelete)) {
            LOG.debug("Cannot find messages to delete [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", messagesToDelete, start, end, finalRecipient);
            return Collections.emptyList();
        }
        LOG.debug("Found messages to delete [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", messagesToDelete, start, end, finalRecipient);

        final List<String> deletedMessages = new ArrayList<>();
        for (UserMessageLogDto message : messagesToDelete) {
            try {
                deleteMessage(message.getMessageId(), message.getMshRole());
                deletedMessages.add(message.getMessageId());
            } catch (Exception e) {
                LOG.error("Failed to delete message [{}] [{}]", e, message.getMessageId(), message.getMshRole());
            }
        }

        LOG.debug("Deleted messages [{}] using start ID_PK date-hour [{}], end ID_PK date-hour [{}] and final recipient [{}]", deletedMessages, start, end, finalRecipient);

        return deletedMessages;
    }

    @Override
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID, DomibusLogger.MDC_MESSAGE_ROLE})
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMessage(String messageId, MSHRole mshRole) {
        LOG.debug("Deleting message [{}]", messageId);

        //add messageId to MDC map
        if (isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        if (mshRole != null) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ROLE, mshRole.name());
        }
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId, mshRole);
        final SignalMessage signalMessage = signalMessageDao.findByUserMessageIdWithUserMessage(messageId, mshRole);
        final UserMessage userMessage;
        if (signalMessage == null) {
            LOG.debug("No signalMessage is present for [{}]", messageId);
            userMessage = userMessageLog.getUserMessage();
        } else {
            userMessage = signalMessage.getUserMessage();
        }
        backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);

        partInfoService.clearPayloadData(userMessage.getEntityId());
        userMessageLog.setDeleted(new Date());

        if (MessageStatus.ACKNOWLEDGED != userMessageLog.getMessageStatus() &&
                MessageStatus.ACKNOWLEDGED_WITH_WARNING != userMessageLog.getMessageStatus()) {
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
        }

        userMessageLogService.setSignalMessageAsDeleted(signalMessage);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMessages(List<UserMessageLogDto> userMessageLogs) {
        em.unwrap(Session.class)
                .setJdbcBatchSize(BATCH_SIZE);

        List<Long> ids = userMessageLogs
                .stream()
                .map(UserMessageLogDto::getEntityId)
                .collect(Collectors.toList());

        List<String> userMessageIds = userMessageLogs
                .stream()
                .map(UserMessageLogDto::getMessageId)
                .collect(Collectors.toList());

        deleteMessages(ids, userMessageIds);

        backendNotificationService.notifyMessageDeleted(userMessageLogs);
        em.flush();
    }

    public void deleteMessages(List<Long> ids, List<String> userMessageIds) {

        LOG.debug("Deleting [{}] user messages", ids.size());
        LOG.trace("Deleting user messages [{}]", ids);

        List<String> filenames = partInfoService.findFileSystemPayloadFilenames(ids);
        partInfoService.deletePayloadFiles(filenames);

        em.flush();
        int deleteResult = userMessageLogDao.deleteMessageLogs(ids);
        LOG.info("Deleted [{}] userMessageLogs.", deleteResult);
        deleteResult = signalMessageLogDao.deleteMessageLogs(ids);
        LOG.info("Deleted [{}] signalMessageLogs.", deleteResult);
        deleteResult = signalMessageRawEnvelopeDao.deleteMessages(ids);
        LOG.info("Deleted [{}] signalMessageRaws.", deleteResult);
        deleteResult = receiptDao.deleteReceipts(ids);
        LOG.info("Deleted [{}] receipts.", deleteResult);
        deleteResult = signalMessageDao.deleteMessages(ids);
        LOG.info("Deleted [{}] signalMessages.", deleteResult);
        deleteResult = userMessageRawEnvelopeDao.deleteMessages(ids);
        LOG.info("Deleted [{}] userMessageRaws.", deleteResult);
        deleteResult = messageAttemptDao.deleteAttemptsByMessageIds(ids);
        LOG.info("Deleted [{}] attempts.", deleteResult);


        deleteResult = errorLogService.deleteErrorLogsByMessageIdInError(userMessageIds);
        LOG.info("Deleted [{}] deleteErrorLogsByMessageIdInError.", deleteResult);
        deleteResult = messageAcknowledgementDao.deleteMessageAcknowledgementsByMessageIds(ids);
        LOG.info("Deleted [{}] deleteMessageAcknowledgementsByMessageIds.", deleteResult);


        deleteResult = userMessageDao.deleteMessages(ids);
        LOG.info("Deleted [{}] userMessages.", deleteResult);

    }

    public void checkCanGetMessageContent(String messageId, MSHRole mshRole) {
        UserMessageLog message = userMessageLogDao.findByMessageId(messageId, mshRole);
        if (message == null) {
            throw new MessagingException("No message found for message id: " + messageId, null);
        }
        if (message.getDeleted() != null) {
            LOG.info("Could not find message content for message: [{}]", messageId);
            throw new MessagingException("Message content is no longer available for message id: " + messageId, null);
        }
        UserMessage userMessage = userMessageDao.findByMessageId(messageId, message.getMshRole().getRole());
        Long contentLength = partInfoService.findPartInfoTotalLength(userMessage.getEntityId());
        int maxDownLoadSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE);
        if (contentLength > maxDownLoadSize) {
            LOG.warn("Couldn't download the message. The message size exceeds maximum download size limit: " + maxDownLoadSize);
            throw new MessagingException("The message size exceeds maximum download size limit: " + maxDownLoadSize, null);
        }
    }

    @Override
    public byte[] getMessageAsBytes(String messageId, MSHRole mshRole) throws MessageNotFoundException {
        UserMessage userMessage = getUserMessageById(messageId);
        auditService.addMessageDownloadedAudit(messageId);
        final List<PartInfo> partInfoList = partInfoService.findPartInfo(userMessage);
        return messageToBytes(userMessage, partInfoList);
    }

    @Override
    public byte[] getMessageWithAttachmentsAsZip(String messageId, MSHRole mshRole) throws MessageNotFoundException, IOException {
        checkCanGetMessageContent(messageId, mshRole);
        Map<String, InputStream> message = getMessageContentWithAttachments(messageId);
        return zipFiles(message);
    }

    @Override
    public byte[] getMessageEnvelopesAsZip(String messageId, MSHRole mshRole) {
        Map<String, InputStream> envelopes = nonRepudiationService.getMessageEnvelopes(messageId);
        if (envelopes.isEmpty()) {
            LOG.debug("Could not find message envelopes with id [{}].", messageId);
            return new byte[0];
        }
        try {
            return zipFiles(envelopes);
        } catch (IOException e) {
            LOG.warn("Could not zip message envelopes with id [{}].", messageId);
            return new byte[0];
        }
    }

    @Override
    public String getUserMessageEnvelope(String userMessageId, MSHRole mshRole) {
        return nonRepudiationService.getUserMessageEnvelope(userMessageId);
    }

    @Override
    public String getSignalMessageEnvelope(String userMessageId, MSHRole mshRole) {
        return nonRepudiationService.getSignalMessageEnvelope(userMessageId);
    }

    @Override
    public UserMessage getByMessageId(String messageId, MSHRole mshRole) throws MessageNotFoundException {
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, mshRole);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }
        return userMessage;
    }

    @Override
    public UserMessage getByMessageEntityId(long messageEntityId) throws MessageNotFoundException {
        final UserMessage userMessage = userMessageDao.findByEntityId(messageEntityId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageEntityId);
        }
        return userMessage;
    }

    protected Map<String, InputStream> getMessageContentWithAttachments(String messageId) throws MessageNotFoundException {

        UserMessage userMessage = getUserMessageById(messageId);

        Map<String, InputStream> result = new HashMap<>();
        final List<PartInfo> partInfos = partInfoService.findPartInfo(userMessage);
        InputStream messageStream = messageToStream(userMessage, partInfos);
        result.put("message.xml", messageStream);

        if (CollectionUtils.isEmpty(partInfos)) {
            LOG.info("No payload info found for message with id [{}]", messageId);
            return result;
        }

        for (PartInfo pInfo : partInfos) {
            if (pInfo.getPayloadDatahandler() == null) {
                try {
                    messageStream.close();
                } catch (IOException e) {
                    LOG.debug("Error encountered while trying to close the message input stream.", e);
                }
                throw new MessagingException("Could not find attachment for [" + pInfo.getHref() + "]", null);
            }
            try {
                result.put(DomibusStringUtil.sanitizeFileName(getPayloadName(pInfo)), pInfo.getPayloadDatahandler().getInputStream());
            } catch (IOException e) {
                throw new MessagingException("Error getting input stream for attachment [" + pInfo.getHref() + "]", e);
            }
        }

        auditService.addMessageDownloadedAudit(messageId);

        return result;
    }

    protected UserMessage getUserMessageById(String messageId) throws MessageNotFoundException {
        // what mshRole? receive it as param?
        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(messageId);
        }

        return userMessage;
    }

    protected InputStream messageToStream(UserMessage userMessage, List<PartInfo> partInfoList) {
        return new ByteArrayInputStream(messageToBytes(userMessage, partInfoList));
    }

    protected byte[] messageToBytes(UserMessage userMessage, List<PartInfo> partInfoList) {
        return messageConverterService.getAsByteArray(userMessage, partInfoList);
    }

    protected String getPayloadName(PartInfo info) {
        if (StringUtils.isEmpty(info.getHref())) {
            return "bodyload.xml";
        }
        String extension = getPayloadExtension(info);
        String messagePayloadNameWithExtension = info.getHref() + "Payload" + extension;

        if (!info.getHref().contains("cid:")) {
            LOG.warn("PayloadId does not contain \"cid:\" prefix [{}]", info.getHref());
            return messagePayloadNameWithExtension;
        }

        for (PartProperty property : info.getPartProperties()) {
            if (StringUtils.equals(property.getName(), PAYLOAD_NAME)) {
                LOG.debug("Payload Name for cid [{}] is [{}]", info.getHref(), property.getName());
                return property.getValue();
            }
        }

        return messagePayloadNameWithExtension.replace("cid:", "");
    }

    protected String getPayloadExtension(PartInfo info) {
        String extension = null;
        for (PartProperty property : info.getPartProperties()) {
            if (StringUtils.equalsIgnoreCase(property.getName(), MIME_TYPE)) {
                extension = fileServiceUtil.getExtension(property.getValue());
                LOG.debug("Payload extension for cid [{}] is [{}]", info.getHref(), extension);
            }
        }
        return extension;
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

    @Override
    public Map<String, String> getProperties(Long messageEntityId) {
        HashMap<String, String> properties = new HashMap<>();
        final UserMessage userMessage = userMessageDao.read(messageEntityId);
        final Set<MessageProperty> propertiesForMessageId = userMessage.getMessageProperties();
        propertiesForMessageId.forEach(property -> properties.put(property.getName(), property.getValue()));
        return properties;
    }

}
