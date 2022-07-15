package eu.domibus.core.message;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.MessageInfo;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.FileServiceUtil;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomibusCoreMapper;
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
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.web.rest.ro.MessageLogRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import javax.persistence.EntityManager;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES;
import static eu.domibus.core.message.UserMessageDefaultService.BATCH_SIZE;
import static eu.domibus.core.message.UserMessageDefaultService.PAYLOAD_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu, Soumya
 * @since 3.3
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "ConstantConditions"})
@RunWith(JMockit.class)
public class UserMessageDefaultServiceTest {

    public static final String MESSAGE_ID = "1000";

    @Tested
    UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private Queue sendLargeMessageQueue;

    @Injectable
    private Queue splitAndJoinQueue;

    @Injectable
    private Queue sendPullReceiptQueue;

    @Injectable
    private Queue retentionMessageQueue;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private UserMessageLogDefaultService userMessageLogService;

    @Injectable
    private UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    protected RoutingService routingService;

    @Injectable
    protected PartInfoService partInfoService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    DomibusCoreMapper coreMapper;

    @Injectable
    PModeService pModeService;

    @Injectable
    PModeServiceHelper pModeServiceHelper;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    MessageGroupDao messageGroupDao;

    @Injectable
    UserMessageFactory userMessageFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    MessageConverterService messageConverterService;

    @Injectable
    AuditService auditService;

    @Injectable
    UserMessagePriorityService userMessagePriorityService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DateUtil dateUtil;

//    @Injectable
//    private MessageInfoDao messageInfoDao;

    @Injectable
    private MessageAttemptDao messageAttemptDao;

    @Injectable
    private ErrorLogService errorLogService;

    @Injectable
    private MessagePropertyDao propertyDao;

    @Injectable
    private MessageAcknowledgementDao messageAcknowledgementDao;

    @Injectable
    NonRepudiationService nonRepudiationService;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    MessageCoreMapper messageCoreMapper;

    @Injectable
    PartInfoDao partInfoDao;

    @Injectable
    MessagePropertyDao messagePropertyDao;

    @Injectable
    private ReprogrammableService reprogrammableService;

    @Injectable
    private SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    @Injectable
    private UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;

    @Injectable
    private ReceiptDao receiptDao;

    @Injectable
    UserMessageDefaultRestoreService userMessageDefaultRestoreService;

    @Injectable
    EntityManager em;

    @Injectable
    private MessagesLogService messagesLogService;

    @Injectable
    private FileServiceUtil fileServiceUtil;

    @Test
    public void testGetFinalRecipient(@Injectable final UserMessage userMessage) {
        final String messageId = "1";

        new Expectations() {{

        }};

        userMessageDefaultService.getFinalRecipient(messageId, MSHRole.SENDING);

        new Verifications() {{
            userMessageServiceHelper.getFinalRecipient(userMessage);
        }};
    }

    @Test
    public void testGetFinalRecipientWhenNoMessageIsFound(@Injectable final UserMessage userMessage) {
        final String messageId = "1";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = null;
        }};

        Assert.assertNull(userMessageDefaultService.getFinalRecipient(messageId, MSHRole.SENDING));
    }

    @Test
    public void testFailedMessages(@Injectable final UserMessage userMessage) {
        final String finalRecipient = "C4";

        userMessageDefaultService.getFailedMessages(finalRecipient, null);

        new Verifications() {{
            userMessageLogDao.findFailedMessages(finalRecipient, null);
        }};
    }

    @Test
    public void testGetFailedMessageElapsedTime(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";
        final Date failedDate = new Date();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = failedDate;
        }};

        final Long failedMessageElapsedTime = userMessageDefaultService.getFailedMessageElapsedTime(messageId);
        assertNotNull(failedMessageElapsedTime);
    }

    @Test(expected = UserMessageException.class)
    public void testGetFailedMessageElapsedTimeWhenFailedDateIsNull(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = null;
        }};

        userMessageDefaultService.getFailedMessageElapsedTime(messageId);
    }

    @Test
    public void testScheduleSending(@Injectable final JmsMessage jmsMessage,
                                    @Injectable DispatchMessageCreator dispatchMessageCreator,
                                    @Injectable UserMessageLog userMessageLog,
                                    @Injectable UserMessage userMessage) {
        final String messageId = "1";
        Long messageEntityId = 1L;

        new Expectations(userMessageDefaultService) {{
            userMessage.getMessageId();
            result = messageId;

            userMessage.getEntityId();
            result = messageEntityId;

            userMessageDefaultService.getDispatchMessageCreator(messageId, messageEntityId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage();
            result = jmsMessage;

            userMessageDefaultService.scheduleSending(userMessage, userMessageLog, jmsMessage);
            times = 1;

        }};

        userMessageDefaultService.scheduleSending(userMessage, userMessageLog);

        new FullVerifications() {
        };

    }

    @Test
    public void testSchedulePullReceiptSending(@Injectable final JmsMessage jmsMessage) {
        final String messageId = "1";
        final String pModeKey = "pModeKey";


        userMessageDefaultService.scheduleSendingPullReceipt(messageId, pModeKey);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, sendPullReceiptQueue);
        }};

    }

    public void testFailedMessageWhenNoMessageIsFound() {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = null;
        }};

        try {
            userMessageDefaultService.getFailedMessage(messageId);
        } catch (MessageNotFoundException e) {
            //OK
        }
    }

    @Test(expected = UserMessageException.class)
    public void testFailedMessageWhenStatusIsNotFailed(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.RECEIVING);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.RECEIVED;
        }};

        userMessageDefaultService.getFailedMessage(messageId);
    }

    @Test
    public void testGetFailedMessage(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_FAILURE;
        }};

        final UserMessageLog failedMessage = userMessageDefaultService.getFailedMessage(messageId);
        Assert.assertNotNull(failedMessage);
    }

    @Test
    public void testDeleteMessaged(@Injectable UserMessageLog userMessageLog,
                                   @Injectable UserMessage userMessage) {
        final String messageId = "1";

        new Expectations() {{
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
        }};

        userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);

        new Verifications() {{
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
        }};
    }

    @Test
    public void testDeleteMessages(@Injectable UserMessageLogDto uml1,
                                   @Injectable UserMessageLogDto uml2,
                                   @Injectable Session session) {
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        List<String> filenames = new ArrayList<>();
        filenames.add("file1");
        new Expectations() {{
            uml1.getEntityId();
            result = 1L;
            uml2.getEntityId();
            result = 2L;
            uml1.getMessageId();
            result = "1L";
            uml2.getMessageId();
            result = "2L";
            em.unwrap(Session.class);
            result = session;

            partInfoService.findFileSystemPayloadFilenames((List<Long>) any);
            result = filenames;

            userMessageLogDao.deleteMessageLogs((List<Long>) any);
            result = 1;
            signalMessageLogDao.deleteMessageLogs((List<Long>) any);
            result = 1;
            signalMessageRawEnvelopeDao.deleteMessages((List<Long>) any);
            result = 1;
            receiptDao.deleteReceipts((List<Long>) any);
            result = 1;
            signalMessageDao.deleteMessages((List<Long>) any);
            result = 1;
            userMessageRawEnvelopeDao.deleteMessages((List<Long>) any);
            result = 1;
            messageAttemptDao.deleteAttemptsByMessageIds((List<Long>) any);
            result = 1;

            errorLogService.deleteErrorLogsByMessageIdInError((List<String>) any);
            result = 1;

            messageAcknowledgementDao.deleteMessageAcknowledgementsByMessageIds((List<Long>) any);
            result = 1;

            userMessageDao.deleteMessages((List<Long>) any);
            result = 1;
        }};

        userMessageDefaultService.deleteMessages(userMessageLogDtos);

        new FullVerifications() {{
            session.setJdbcBatchSize(BATCH_SIZE);

            backendNotificationService.notifyMessageDeleted((List<UserMessageLogDto>) any);

            partInfoService.deletePayloadFiles(filenames);

            em.flush();
            times = 2;
        }};
    }

    @Test
    public void marksTheUserMessageAsDeleted(@Injectable UserMessage userMessage,
                                             @Injectable UserMessageLog userMessageLog,
                                             @Injectable SignalMessage signalMessage) {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageIdSafely(messageId, MSHRole.SENDING);
            result = userMessageLog;

            signalMessageDao.findByUserMessageIdWithUserMessage(messageId, MSHRole.SENDING);
            result = signalMessage;

            signalMessage.getUserMessage();
            result = userMessage;

        }};

        userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);

        new FullVerifications() {{
            userMessageLog.setDeleted((Date) any);
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            userMessageLogService.setSignalMessageAsDeleted(signalMessage);
            userMessageLog.getMessageStatus();
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
            times = 1;

            partInfoService.clearPayloadData(userMessage.getEntityId());
        }};
    }

    @Test
    public void marksTheUserMessageAsDeleted_emptySignal(@Injectable UserMessage userMessage,
                                                         @Injectable UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageIdSafely(messageId, MSHRole.SENDING);
            result = userMessageLog;

            signalMessageDao.findByUserMessageIdWithUserMessage(messageId, MSHRole.SENDING);
            result = null;

            userMessageLog.getUserMessage();
            result = userMessage;

            userMessageLog.getMessageStatus();
            result = MessageStatus.DOWNLOADED;

        }};

        userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);

        new FullVerifications() {{
            partInfoService.clearPayloadData(userMessage.getEntityId());
            times = 1;

            userMessageLog.setDeleted((Date) any);
            times = 1;

            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
            times = 1;

            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            times = 1;

            userMessageLogService.setSignalMessageAsDeleted((SignalMessage) null);
            times = 1;

        }};
    }

    @Test
    public void test_sendEnqueued(final @Injectable UserMessageLog userMessageLog,
                                  final @Injectable UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES);
            result = 2;

            userMessageLog.getReceived();
            result = DateUtils.addMinutes(new Date(), -3);

            userMessageLog.getNextAttempt();
            result = null;

            userMessageDao.findByMessageId(messageId);
            result = userMessage;

            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);
            times = 1;

        }};

        //tested method
        userMessageDefaultService.sendEnqueuedMessage(messageId);

        new FullVerifications() {{
            reprogrammableService.setRescheduleInfo(userMessageLog, withAny(new Date()));
            userMessageLogDao.update(userMessageLog);
        }};
    }

    @Test
    public void getUserMessagePriority(@Injectable UserMessage userMessage) {
        String service = "my service";
        String action = "my action";

        new Expectations() {{
            userMessageServiceHelper.getService(userMessage);
            result = service;

            userMessageServiceHelper.getAction(userMessage);
            result = action;
        }};

        userMessageDefaultService.getUserMessagePriority(userMessage);

        new Verifications() {{
            userMessagePriorityService.getPriority(service, action);
        }};
    }


    @Test
    public void deleteFailedMessageTest() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            times = 1;
            userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);
            times = 1;
        }};

        userMessageDefaultService.deleteFailedMessage(messageId);

        new FullVerificationsInOrder(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);
        }};
    }


    @Test
    public void scheduleSendingWithRetryCountTest(@Injectable final JmsMessage jmsMessage,
                                                  @Injectable UserMessageLog userMessageLog,
                                                  @Injectable DispatchMessageCreator dispatchMessageCreator,
                                                  @Injectable UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();
        Long messageEntityId = 1L;

        int retryCount = 3;

        new Expectations(userMessageDefaultService) {{
            userMessageLog.getEntityId();
            result = 10L;

            userMessage.getEntityId();
            result = messageEntityId;

            userMessageDao.read(10L);
            result = userMessage;

            userMessage.getMessageId();
            result = messageId;

            userMessageDefaultService.getDispatchMessageCreator(messageId, messageEntityId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage(retryCount);
            result = jmsMessage;

            userMessageDefaultService.scheduleSending(userMessage, messageId, userMessageLog, jmsMessage);
            times = 1;
        }};

        userMessageDefaultService.scheduleSending(userMessageLog, retryCount);

        new FullVerifications() {
        };
    }

    @Test
    public void scheduleSendingPullReceiptWithRetryCountTest(@Injectable final JmsMessage jmsMessage,
                                                             @Injectable UserMessageService userMessageService) {
        final String messageId = UUID.randomUUID().toString();
        final String pModeKey = "pModeKey";
        final int retryCount = 3;

        userMessageDefaultService.scheduleSendingPullReceipt(messageId, pModeKey, retryCount);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, sendPullReceiptQueue);
        }};
    }

    @Test
    public void scheduleSplitAndJoinReceiveFailedTest(@Injectable final JmsMessage jmsMessage,
                                                      @Injectable UserMessageService userMessageService) {
        final String sourceMessageId = UUID.randomUUID().toString();
        final String groupId = "groupId";
        final String errorCode = "ebms3ErrorCode";
        final String errorDetail = "ebms3ErrorDetail";

        userMessageDefaultService.scheduleSplitAndJoinReceiveFailed(groupId, sourceMessageId, errorCode, errorDetail);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};

    }

    @Test
    public void scheduleSendingSignalErrorTest(@Injectable final JmsMessage jmsMessage,
                                               @Injectable UserMessageService userMessageService) {
        final String messageId = UUID.randomUUID().toString();
        final String pmodeKey = "pmodeKey";
        final String ebMS3ErrorCode = "ebms3ErrorCode";
        final String errorDetail = "ebms3ErrorDetail";

        userMessageDefaultService.scheduleSendingSignalError(messageId, ebMS3ErrorCode, errorDetail, pmodeKey);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSourceMessageReceiptTest(@Injectable final JmsMessage jmsMessage,
                                                 @Injectable UserMessageService userMessageService) {
        final String messageId = UUID.randomUUID().toString();
        final String pmodeKey = "pmodeKey";

        userMessageDefaultService.scheduleSourceMessageReceipt(messageId, pmodeKey);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSourceMessageRejoinTest(@Injectable final JmsMessage jmsMessage,
                                                @Injectable UserMessageService userMessageService) {
        final String groupId = "groupId";
        final String file = "SourceMessageFile";
        final String backendName = "backendName";

        userMessageDefaultService.scheduleSourceMessageRejoin(groupId, file, backendName);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSourceMessageRejoinFileTest(@Injectable final JmsMessage jmsMessage,
                                                    @Injectable UserMessageService userMessageService) {
        final String groupId = "groupId";
        final String backendName = "backendName";

        userMessageDefaultService.scheduleSourceMessageRejoinFile(groupId, backendName);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSetUserMessageFragmentAsFailedTest(@Injectable final JmsMessage jmsMessage,
                                                           @Injectable UserMessageService userMessageService) {
        final String messageId = UUID.randomUUID().toString();

        userMessageDefaultService.scheduleSetUserMessageFragmentAsFailed(messageId, MSHRole.SENDING);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSplitAndJoinSendFailedTest(@Injectable final JmsMessage jmsMessage,
                                                   @Injectable UserMessageService userMessageService) {
        final String groupId = "groupId";
        final String errorDetail = "ebms3ErrorDetail";

        userMessageDefaultService.scheduleSplitAndJoinSendFailed(groupId, errorDetail);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, splitAndJoinQueue);
        }};
    }

    @Test
    public void scheduleSourceMessageSendingTest(@Injectable final JmsMessage jmsMessage,
                                                 @Injectable DispatchMessageCreator dispatchMessageCreator) {
        final String messageId = UUID.randomUUID().toString();
        Long messageEntityId = 1L;

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getDispatchMessageCreator(messageId, messageEntityId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage();
            result = jmsMessage;
        }};

        userMessageDefaultService.scheduleSourceMessageSending(messageId, messageEntityId);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, sendLargeMessageQueue);
        }};
    }

    @Test
    public void testPayloadName(@Injectable final PartInfo partInfoWithBodyload, @Injectable final PartInfo partInfoWithPayload,
                                @Injectable final PartInfo partInfoWithPartProperties, @Injectable PartProperty partProperty) {

        Set<PartProperty> partProperties = new HashSet<>();
        partProperties.add(partProperty);
        new Expectations(userMessageDefaultService) {{
            partInfoWithBodyload.getHref();
            result = null;
            partInfoWithPayload.getHref();
            result = "cid:1234";
            partInfoWithPartProperties.getHref();
            result = "cid:1234";
            partInfoWithPartProperties.getPartProperties();
            result = partProperties;
            partProperty.getName();
            result = PAYLOAD_NAME;
            partProperty.getValue();
            result = "test.txt";
            userMessageDefaultService.getPayloadExtension(partInfoWithPayload);
            result = ".xml";

        }};

        Assert.assertEquals("bodyload.xml", userMessageDefaultService.getPayloadName(partInfoWithBodyload));
        Assert.assertEquals("1234Payload.xml", userMessageDefaultService.getPayloadName(partInfoWithPayload));
        Assert.assertEquals("test.txt", userMessageDefaultService.getPayloadName(partInfoWithPartProperties));
    }

    @Test
    public void getUserMessageById() {
        final String messageId = UUID.randomUUID().toString();
        UserMessage userMessage = new UserMessage();

        new Expectations() {
        };

        UserMessage result = userMessageDefaultService.getUserMessageById(messageId);

        Assert.assertEquals(userMessage, result);
    }

    @Test(expected = MessageNotFoundException.class)
    public void getUserMessageById_notFound() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = null;
        }};

        userMessageDefaultService.getUserMessageById(messageId);

        new Verifications() {{
            auditService.addMessageDownloadedAudit(messageId);
            times = 0;
        }};
    }

    @Test
    public void getMessageInFinalStatus() {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = null;
        }};
        try {
            userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
            Assert.fail();
        } catch (MessageNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("Message [1] does not exist"));
        }

    }

    @Test
    public void getMessageNotInFinalStatus(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getDeleted();
            result = null;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;
        }};

        final UserMessageLog message = userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
        Assert.assertNotNull(message);
    }

    @Test
    public void getMessageNotInFinalStatus_deleted(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";
        Date deleted = new Date();
        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getDeleted();
            result = deleted;

            userMessageLog.getMessageStatus();
            result = MessageStatus.ACKNOWLEDGED;
        }};

        try {
            userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
            fail();
        } catch (MessagingException ex) {
            Assert.assertTrue(ex.getMessage().contains("Message [1] in state [" + MessageStatus.ACKNOWLEDGED.name() + "] is already deleted. Delete time: [" + deleted + "]"));
        }
    }

    @Test
    public void getMessageNotInFinalStatus_FinalState(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getDeleted();
            result = null;

            userMessageLog.getMessageStatus();
            result = MessageStatus.ACKNOWLEDGED;
        }};

        try {
            userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
            fail();
        } catch (MessagingException ex) {
            Assert.assertTrue(ex.getMessage().contains("Message [1] is in final state [" + MessageStatus.ACKNOWLEDGED.name() + "]"));
        }
    }

    @Test
    public void deleteMessageNotInFinalStatus() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
            times = 1;
            userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);
            times = 1;
        }};

        userMessageDefaultService.deleteMessageNotInFinalStatus(messageId, MSHRole.SENDING);

        new FullVerificationsInOrder(userMessageDefaultService) {{
            userMessageDefaultService.getMessageNotInFinalStatus(messageId, MSHRole.SENDING);
            userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);
        }};
    }

    @Test
    public void deleteMessagesDuringPeriod() {
        final String messageId = "1";
        final List<String> messagesToDelete = new ArrayList<>();
        messagesToDelete.add(messageId);

        final String originalUserFromSecurityContext = "C4";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findMessagesToDelete(originalUserFromSecurityContext, 1L, 2L);
            result = messagesToDelete;
            userMessageDefaultService.deleteMessage(messageId, MSHRole.SENDING);
            times = 1;
        }};

        userMessageDefaultService.deleteMessagesDuringPeriod(1L, 2L, originalUserFromSecurityContext);

        new FullVerifications() {
        };
    }

    @Test
    public void test_checkCanDownloadWithMaxDownLoadSize(@Injectable MessageLogRO existingMessage, @Injectable UserMessage userMessage) {
        String messageId = "messageId";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(anyString, (MSHRole)any);
            result = existingMessage;
            existingMessage.getDeleted();
            result = null;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_DOWNLOAD_MAX_SIZE);
            result = 1;
            userMessageDao.findByMessageId(messageId);
            result = userMessage;
            partInfoService.findPartInfoTotalLength(userMessage.getEntityId());
            result = 1000;
        }};

        try {
            userMessageDefaultService.checkCanGetMessageContent(messageId, MSHRole.RECEIVING);
            Assert.fail();
        } catch (MessagingException ex) {
            Assert.assertEquals(ex.getMessage(), "[DOM_001]:The message size exceeds maximum download size limit: 1");
        }
    }

    @Test
    public void test_checkCanDownloadWithDeletedMessage(@Injectable MessageLogRO deletedMessage) {
        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(anyString, (MSHRole)any);
            result = deletedMessage;
            deletedMessage.getDeleted();
            result = new Date();

        }};
        try {
            userMessageDefaultService.checkCanGetMessageContent("messageId", MSHRole.RECEIVING);
            Assert.fail();
        } catch (MessagingException ex) {
            Assert.assertTrue(ex.getMessage().contains("[DOM_001]:Message content is no longer available for message id:"));
        }
    }

    @Test
    public void test_checkCanDownloadWithExistingMessage(@Injectable MessageLogRO existingMessage) {
        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(anyString, (MSHRole)any);
            result = existingMessage;
            existingMessage.getDeleted();
            result = null;
        }};

        userMessageDefaultService.checkCanGetMessageContent("messageId", MSHRole.RECEIVING);
    }

    @Test
    public void test_checkCanDownloadWhenNoMessage() {
        new Expectations() {{
            userMessageLogDao.findByMessageId(anyString, (MSHRole)any);
            result = null;
        }};

        try {
            userMessageDefaultService.checkCanGetMessageContent("messageId", MSHRole.RECEIVING);
            Assert.fail();
        } catch (MessagingException ex) {
            Assert.assertEquals(ex.getMessage(), "[DOM_001]:No message found for message id: messageId");
        }
    }
}
