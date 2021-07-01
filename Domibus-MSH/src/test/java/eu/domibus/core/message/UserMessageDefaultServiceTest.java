package eu.domibus.core.message;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.MessageInfo;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.converter.MessageCoreMapper;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.jms.DelayedDispatchMessageCreator;
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
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.scheduler.ReprogrammableService;
import eu.domibus.messaging.MessagingProcessingException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu, Soumya
 * @since 3.3
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserMessageDefaultServiceTest {

  private static final long SYSTEM_DATE = new Date().getTime();
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
    protected UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    MessageGroupDao messageGroupDao;

    @Injectable
    UserMessageFactory userMessageFactory;

    @Injectable
    DatabaseMessageHandler databaseMessageHandler;

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

    @Injectable
    private MessageInfoDao messageInfoDao;

    @Injectable
    private MessageAttemptDao messageAttemptDao;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    private MessagePropertyDao propertyDao;

    @Injectable
    private UIMessageDao uiMessageDao;

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

    @Injectable(JPAConstants.PERSISTENCE_UNIT_NAME)
    EntityManager em;

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void createMessagingForFragment(@Injectable UserMessage sourceMessage,
                                           @Injectable MessageGroupEntity messageGroupEntity,
                                           @Injectable UserMessage userMessageFragment) throws MessagingProcessingException {
        String backendName = "mybackend";

        final String fragment1 = "fragment1";

        new Expectations() {{
            userMessageFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, 1L, fragment1);
            result = userMessageFragment;
        }};

        userMessageDefaultService.createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragment1, 1);

        new Verifications() {{
            userMessageFactory.createUserMessageFragment(sourceMessage, messageGroupEntity, 1L, fragment1);
//            databaseMessageHandler.submitMessageFragment(sourceMessage, userMessageFragment, backendName);
        }};
    }

    @Test
    public void createMessageFragments(@Injectable UserMessage sourceMessage,
                                       @Injectable MessageGroupEntity messageGroupEntity
    ) throws MessagingProcessingException {
        String messageId = "123";
        String backendName = "mybackend";

        List<String> fragmentFiles = new ArrayList<>();
        final String fragment1 = "fragment1";
        fragmentFiles.add(fragment1);


        new Expectations(userMessageDefaultService) {{
            sourceMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findBackendForMessageId(messageId);
            result = backendName;


            userMessageDefaultService.createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, anyString, anyInt);
        }};

        userMessageDefaultService.createMessageFragments(sourceMessage, messageGroupEntity, fragmentFiles);

        new Verifications() {{
            messageGroupDao.create(messageGroupEntity);

            userMessageDefaultService.createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragment1, 1);
        }};
    }

    @Test
    public void testGetFinalRecipient(@Injectable final UserMessage userMessage) {
        final String messageId = "1";

        new Expectations() {{

        }};

        userMessageDefaultService.getFinalRecipient(messageId);

        new Verifications() {{
            userMessageServiceHelper.getFinalRecipient(userMessage);
        }};
    }

    @Test
    public void testGetFinalRecipientWhenNoMessageIsFound(@Injectable final UserMessage userMessage) {
        final String messageId = "1";

        new Expectations() {{

        }};

        Assert.assertNull(userMessageDefaultService.getFinalRecipient(messageId));
    }

    @Test
    public void testFailedMessages(@Injectable final UserMessage userMessage) {
        final String finalRecipient = "C4";

        userMessageDefaultService.getFailedMessages(finalRecipient);

        new Verifications() {{
            userMessageLogDao.findFailedMessages(finalRecipient);
        }};
    }

    @Test
    public void testGetFailedMessageElapsedTime(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";
        final Date failedDate = new Date();

        new CurrentTimeMillisMock();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = failedDate;
        }};

        final Long failedMessageElapsedTime = userMessageDefaultService.getFailedMessageElapsedTime(messageId);
        assertEquals(SYSTEM_DATE - failedDate.getTime(), (long) failedMessageElapsedTime);
    }

    @Test(expected = UserMessageException.class)
    public void testGetFailedMessageElapsedTimeWhenFailedDateIsNull(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new CurrentTimeMillisMock();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getFailed();
            result = null;
        }};

        userMessageDefaultService.getFailedMessageElapsedTime(messageId);
    }



    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testScheduleSending(@Injectable final JmsMessage jmsMessage,
                                    @Mocked DispatchMessageCreator dispatchMessageCreator,
                                    @Injectable UserMessageLog userMessageLog,
                                    @Injectable UserMessage userMessage) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            new DispatchMessageCreator(messageId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage();
            result = jmsMessage;

        }};

        userMessageDefaultService.scheduleSending(userMessage, userMessageLog);

        new Verifications() {{
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog, jmsMessage);
        }};

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

    @Test(expected = UserMessageException.class)
    public void testFailedMessageWhenNoMessageIsFound(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = null;
        }};

        userMessageDefaultService.getFailedMessage(messageId);
    }

    @Test(expected = UserMessageException.class)
    public void testFailedMessageWhenStatusIsNotFailed(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.RECEIVED;
        }};

        userMessageDefaultService.getFailedMessage(messageId);
    }

    @Test
    public void testGetFailedMessage(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
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

        new Expectations(userMessageDefaultService) {{
            backendNotificationService.notifyMessageDeleted(userMessage,userMessageLog);
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new Verifications() {{
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
        }};
    }

    @Test
    @Ignore
    public void testDeleteMessages(@Injectable UserMessageLogDto uml1, @Injectable UserMessageLogDto uml2, @Mocked Session session) {
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);

        new Expectations() {{
           em.unwrap(Session.class); result = session;
        }};

        userMessageDefaultService.deleteMessages(userMessageLogDtos);

        new Verifications() {{
            backendNotificationService.notifyMessageDeleted((List<UserMessageLogDto>) any);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void marksTheUserMessageAsDeleted(@Injectable Messaging messaging,
                                             @Injectable UserMessage userMessage,
                                             @Injectable UserMessageLog userMessageLog,
                                             @Injectable SignalMessage signalMessage,
                                             @Injectable MessageInfo messageInfo) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = userMessageLog;

            messaging.getSignalMessage();
            result = signalMessage;
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new FullVerifications() {{
            userMessageLog.setDeleted((Date)any);
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            userMessageLogService.setSignalMessageAsDeleted(signalMessage);
            userMessageLog.getMessageStatus();
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void marksTheUserMessageAsDeleted_emptySignal(@Injectable Messaging messaging,
                                                         @Injectable UserMessage userMessage,
                                                         @Injectable UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            messaging.getUserMessage();
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = userMessageLog;

            messaging.getSignalMessage();
            result = null;
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new FullVerifications() {{
            userMessageLog.setDeleted((Date)any);
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            userMessageLogService.setSignalMessageAsDeleted((SignalMessage) null);
            userMessageLog.getMessageStatus();
            backendNotificationService.notifyMessageDeleted(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test(expected = UserMessageException.class)
    public void test_ResendFailedOrSendEnqueuedMessage_StatusSendEnqueued(final @Mocked UserMessageLog userMessageLog) throws Exception {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;
        }};

        //tested method
        userMessageDefaultService.resendFailedOrSendEnqueuedMessage(messageId);


        new FullVerifications(userMessageDefaultService) {{
            String messageIdActual;
            userMessageDefaultService.sendEnqueuedMessage(messageIdActual = withCapture());
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void test_ResendFailedOrSendEnqueuedMessage_MessageNotFound() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = null;
        }};

        try {
            //tested method
            userMessageDefaultService.resendFailedOrSendEnqueuedMessage(messageId);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(UserMessageException.class, e.getClass());
        }

        new FullVerifications(userMessageDefaultService) {
        };
    }

    @Test
    public void test_sendEnqueued(final @Mocked UserMessageLog userMessageLog, final @Mocked UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RESEND_BUTTON_ENABLED_RECEIVED_MINUTES);
            result = 2;

            userMessageLog.getReceived();
            result = DateUtils.addMinutes(new Date(), -3);

            userMessageLog.getNextAttempt();
            result = null;

        }};

        //tested method
        userMessageDefaultService.sendEnqueuedMessage(messageId);

        new FullVerifications(userMessageDefaultService) {{
            reprogrammableService.setRescheduleInfo(userMessageLog, withAny(new Date()));
            userMessageLogDao.update(userMessageLog);
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);
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

    private static class CurrentTimeMillisMock extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE;
        }
    }

    @Test
    public void deleteFailedMessageTest() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            times = 1;
            userMessageDefaultService.deleteMessage(messageId);
            times = 1;
        }};

        userMessageDefaultService.deleteFailedMessage(messageId);

        new FullVerificationsInOrder(userMessageDefaultService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            userMessageDefaultService.deleteMessage(messageId);
        }};
    }

    @Test
    public void scheduleSendingWithDelayTest(@Injectable final JmsMessage jmsMessage,
                                             @Mocked DelayedDispatchMessageCreator delayedDispatchMessageCreator,
                                             @Injectable UserMessageLog userMessageLog,
                                             @Injectable UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();
        Long delay = 1L;
        boolean isSplitAndJoin = false;

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageIdSafely(messageId);
            result = userMessageLog;

            new DelayedDispatchMessageCreator(messageId, delay);
            result = delayedDispatchMessageCreator;

            delayedDispatchMessageCreator.createMessage();
            result = jmsMessage;

        }};

        userMessageDefaultService.scheduleSending(messageId, delay);

        new Verifications() {{
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog, new DelayedDispatchMessageCreator(messageId, delay).createMessage());
            times = 1;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void scheduleSendingWithRetryCountTest(@Injectable final JmsMessage jmsMessage,
                                                  @Injectable UserMessageLog userMessageLog,
                                                  @Mocked DispatchMessageCreator dispatchMessageCreator,
                                                  @Injectable UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();

        int retryCount = 3;
        boolean isSplitAndJoin = false;

        new Expectations(userMessageDefaultService) {{
            new DispatchMessageCreator(messageId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage(retryCount);
            result = jmsMessage;

        }};

        userMessageDefaultService.scheduleSending(userMessageLog, retryCount);

        new Verifications() {{
            userMessageDefaultService.scheduleSending(userMessage, messageId, userMessageLog, new DispatchMessageCreator(messageId).createMessage(retryCount));
            times = 1;
        }};
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

        userMessageDefaultService.scheduleSetUserMessageFragmentAsFailed(messageId);

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
                                                 @Mocked DispatchMessageCreator dispatchMessageCreator) {
        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            new DispatchMessageCreator(messageId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage();
            result = jmsMessage;
        }};

        userMessageDefaultService.scheduleSourceMessageSending(messageId);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, sendLargeMessageQueue);
        }};
    }

    @Test
    public void testPayloadName(@Mocked final PartInfo partInfoWithBodyload, @Mocked final PartInfo partInfoWithPayload) {
        new Expectations() {{
            partInfoWithBodyload.getHref();
            result = null;
            partInfoWithPayload.getHref();
            result = "cid:1234";
        }};
        Assert.assertEquals("bodyload", userMessageDefaultService.getPayloadName(partInfoWithBodyload));
        Assert.assertEquals("1234", userMessageDefaultService.getPayloadName(partInfoWithPayload));
    }

    @Test
    public void getUserMessageById() {
        final String messageId = UUID.randomUUID().toString();
        UserMessage userMessage = new UserMessage();

        new Expectations() {{
        }};

        UserMessage result = userMessageDefaultService.getUserMessageById(messageId);

        Assert.assertEquals(userMessage, result);
    }

    @Test(expected = MessageNotFoundException.class)
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void getUserMessageById_notFound() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
        }};

        userMessageDefaultService.getUserMessageById(messageId);

        new Verifications() {{
            auditService.addMessageDownloadedAudit(messageId);
            times = 0;
        }};
    }

}