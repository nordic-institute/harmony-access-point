package eu.domibus.core.message;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
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
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.messaging.MessagingProcessingException;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ACTION_RESEND_WAIT_MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private MessagingDao messagingDao;

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
    DomainCoreConverter domainConverter;

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
    private UIMessageDao uiMessageDao;

    @Injectable
    private MessageAcknowledgementDao messageAcknowledgementDao;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    EntityManager em;

    @Injectable
    UserMessageDefaultRestoreService restoreService;

    @Test
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
            databaseMessageHandler.submitMessageFragment(userMessageFragment, backendName);
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
            sourceMessage.getMessageInfo().getMessageId();
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
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

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
            messagingDao.findUserMessageByMessageId(messageId);
            result = null;

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
    public void testScheduleSending(@Injectable final JmsMessage jmsMessage,
                                    @Mocked DispatchMessageCreator dispatchMessageCreator,
                                    @Injectable UserMessageLog userMessageLog,
                                    @Injectable UserMessage userMessage) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            userMessageLog.getMessageId();
            result = messageId;

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

    @Test
    public void testRestoreFailedMessagesDuringPeriodWhenAPreviousMessageIsFailing() {
        final String finalRecipient = "C4";
        final Date startDate = new Date();
        final Date endDate = new Date();

        final String failedMessage1 = "1";
        final String failedMessage2 = "2";
        final List<String> failedMessages = new ArrayList<>();
        failedMessages.add(failedMessage1);
        failedMessages.add(failedMessage2);

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findFailedMessages(finalRecipient, startDate, endDate);
            result = failedMessages;

            restoreService.restoreFailedMessage(failedMessage1);

            restoreService.restoreFailedMessage(failedMessage2);
            result = new RuntimeException("Problem restoring message 2");
        }};

        final List<String> restoredMessages = userMessageDefaultService.restoreFailedMessagesDuringPeriod(startDate, endDate, finalRecipient);
        assertNotNull(restoredMessages);
        assertEquals(1, restoredMessages.size());
        assertEquals(failedMessage1, restoredMessages.iterator().next());
    }

    @Test
    public void testRestoreFailedMessagesDuringPeriod() {
        final String finalRecipient = "C4";
        final Date startDate = new Date();
        final Date endDate = new Date();

        final String failedMessage1 = "1";
        final String failedMessage2 = "2";
        final List<String> failedMessages = new ArrayList<>();
        failedMessages.add(failedMessage1);
        failedMessages.add(failedMessage2);

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findFailedMessages(finalRecipient, startDate, endDate);
            result = failedMessages;

            restoreService.restoreFailedMessage(anyString);
        }};

        final List<String> restoredMessages = userMessageDefaultService.restoreFailedMessagesDuringPeriod(startDate, endDate, finalRecipient);
        assertNotNull(restoredMessages);
        assertEquals(restoredMessages, failedMessages);
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
    public void testDeleteMessaged(@Injectable UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            backendNotificationService.notifyMessageDeleted((String) any, userMessageLog);
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new Verifications() {{
            backendNotificationService.notifyMessageDeleted(messageId, userMessageLog);
        }};
    }

    @Test
    public void testDeleteMessages(@Mocked Session s, @Mocked EntityManager em1, @Injectable UserMessageLogDto uml1, @Injectable UserMessageLogDto uml2) {
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);

        new Expectations() {{
            em1.unwrap(Session.class);
            result = s;
        }};

        Deencapsulation.setField(userMessageDefaultService, "em", em1);
        userMessageDefaultService.deleteMessages(userMessageLogDtos);

        new Verifications() {{
            backendNotificationService.notifyMessageDeleted((List<UserMessageLogDto>) any);
        }};
    }

    @Test
    public void testDeleteMessages_emptySignal(@Mocked Session s, @Mocked EntityManager em1,
                                               @Injectable UserMessageLogDto uml1,
                                               @Injectable UserMessageLogDto uml2,
                                               @Injectable Messaging messaging) {
        List<UserMessageLogDto> userMessageLogDtos = Arrays.asList(uml1, uml2);
        List<Messaging> messagings = new ArrayList<>();
        messagings.add(messaging);
        new Expectations() {{
            em1.unwrap(Session.class);
            result = s;
            messagingDao.findMessagings((List<String>) any);
            result = messagings;
            messaging.getSignalMessage();
            result = null;
        }};

        Deencapsulation.setField(userMessageDefaultService, "em", em1);
        userMessageDefaultService.deleteMessages(userMessageLogDtos);

        new Verifications() {{
            signalMessageLogDao.deleteMessageLogs(Collections.emptyList());
            backendNotificationService.notifyMessageDeleted((List<UserMessageLogDto>) any);
        }};
    }

    @Test
    public void marksTheUserMessageAsDeleted(@Injectable Messaging messaging,
                                             @Injectable UserMessage userMessage,
                                             @Injectable UserMessageLog userMessageLog,
                                             @Injectable SignalMessage signalMessage,
                                             @Injectable MessageInfo messageInfo) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            messagingDao.findMessageByMessageId(messageId);
            result = messaging;

            messaging.getUserMessage();
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = userMessageLog;

            messaging.getSignalMessage();
            result = signalMessage;
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new FullVerifications() {{
            messagingDao.clearPayloadData(userMessage);
            userMessageLog.setDeleted((Date) any);
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            userMessageLogService.setSignalMessageAsDeleted(signalMessage);
            userMessageLog.getMessageStatus();
            backendNotificationService.notifyMessageDeleted(messageId, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void marksTheUserMessageAsDeleted_emptySignal(@Injectable Messaging messaging,
                                                         @Injectable UserMessage userMessage,
                                                         @Injectable UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(userMessageDefaultService) {{
            messagingDao.findMessageByMessageId(messageId);
            result = messaging;

            messaging.getUserMessage();
            result = userMessage;

            userMessageLogDao.findByMessageIdSafely(messageId);
            result = userMessageLog;

            messaging.getSignalMessage();
            result = null;
        }};

        userMessageDefaultService.deleteMessage(messageId);

        new FullVerifications() {{
            messagingDao.clearPayloadData(userMessage);
            userMessageLog.setDeleted((Date) any);
            userMessageLogService.setMessageAsDeleted(userMessage, userMessageLog);
            userMessageLogService.setSignalMessageAsDeleted((SignalMessage) null);
            userMessageLog.getMessageStatus();
            backendNotificationService.notifyMessageDeleted(messageId, userMessageLog);
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
    public void test_ResendFailedOrSendEnqueuedMessage_StatusFailed(final @Mocked UserMessageLog userMessageLog) {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_FAILURE;
        }};

        //tested method
        userMessageDefaultService.resendFailedOrSendEnqueuedMessage(messageId);

        new FullVerifications(userMessageDefaultService) {{
            String messageIdActual;
            restoreService.restoreFailedMessage(messageIdActual = withCapture());
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

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ACTION_RESEND_WAIT_MINUTES);
            result = 2;

            userMessageLog.getReceived();
            result = DateUtils.addMinutes(new Date(), -3);

            userMessageLog.getNextAttempt();
            result = null;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};

        //tested method
        userMessageDefaultService.sendEnqueuedMessage(messageId);

        new FullVerifications(userMessageDefaultService) {{
            userMessageLog.setNextAttempt(withAny(new Date()));
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

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};

        userMessageDefaultService.scheduleSending(messageId, delay);

        new Verifications() {{
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog, new DelayedDispatchMessageCreator(messageId, delay).createMessage());
            times = 1;
        }};
    }

    @Test
    public void scheduleSendingWithRetryCountTest(@Injectable final JmsMessage jmsMessage,
                                                  @Injectable UserMessageLog userMessageLog,
                                                  @Mocked DispatchMessageCreator dispatchMessageCreator,
                                                  @Injectable UserMessage userMessage) {
        final String messageId = UUID.randomUUID().toString();

        int retryCount = 3;
        boolean isSplitAndJoin = false;

        new Expectations(userMessageDefaultService) {{
            userMessageLog.getMessageId();
            result = messageId;

            new DispatchMessageCreator(messageId);
            result = dispatchMessageCreator;

            dispatchMessageCreator.createMessage(retryCount);
            result = jmsMessage;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
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
            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};

        UserMessage result = userMessageDefaultService.getUserMessageById(messageId);

        Assert.assertEquals(userMessage, result);
    }

    @Test(expected = MessageNotFoundException.class)
    public void getUserMessageById_notFound() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            messagingDao.findUserMessageByMessageId(messageId);
            result = null;
        }};

        userMessageDefaultService.getUserMessageById(messageId);

        new Verifications() {{
            auditService.addMessageDownloadedAudit(messageId);
            times = 0;
        }};
    }

    @Test
    public void restoreSendEnqueuedMessagesDuringPeriod() {
        final String finalRecipient = "C4";
        final Date startDate = new Date();
        final Date endDate = new Date();

        final String sendEnqueuedMessage1 = "1";
        final String sendEnqueuedMessage2 = "2";
        final List<String> sendEnqueuedMessages = new ArrayList<>();
        sendEnqueuedMessages.add(sendEnqueuedMessage1);
        sendEnqueuedMessages.add(sendEnqueuedMessage2);

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findSendEnqueuedMessages(finalRecipient, startDate, endDate);
            result = sendEnqueuedMessages;

            restoreService.restoreSendEnqueuedMessage(anyString);
        }};

        final List<String> restoredMessages = userMessageDefaultService.restoreSendEnqueuedMessagesDuringPeriod(startDate, endDate, finalRecipient);
        assertNotNull(restoredMessages);
        assertEquals(restoredMessages, sendEnqueuedMessages);
    }

}