package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.*;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.MapMessage;
import javax.jms.Queue;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class MessageRetentionServiceTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserMessageServiceHelper userMessageServiceHelper;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue retentionMessageQueue;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private JsonUtil jsonUtil;

    @Tested
    MessageRetentionDefaultService messageRetentionService;

    final String mpc1 = "mpc1";
    final String mpc2 = "mpc2";
    final int retentionTime = 60;
    final int maxBatch = 1000;
    final Integer expiredDownloadedMessagesLimit = 10;
    final Integer expiredNotDownloadedMessagesLimit = 20;
    final Integer expiredSentMessagesLimit = 30;
    final Integer expiredPayloadDeletedMessagesLimit = 30;
    List<UserMessageLogDto> expiredMessages;
    final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

    @Before
    public void init() {
        expiredMessages = new ArrayList<>();
        UserMessageLogDto uml1 = new UserMessageLogDto("abc", null, "ws");

        UserMessageLogDto uml2 = new UserMessageLogDto("def", null, "ws");

        expiredMessages.add(uml1);
        expiredMessages.add(uml2);

    }

    @Test
    public void scheduleDeleteMessagesDeleteMetadata() {
        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            pModeProvider.getRetentionMaxBatchByMpcURI(mpc1, domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE));
            result = maxBatch;

            messageRetentionService.scheduleDeleteMessagesByMessageLog(expiredMessages, maxBatch);
        }};

        messageRetentionService.scheduleDeleteMessages(expiredMessages, mpc1);
    }

    @Test
    public void scheduleDeleteMessages() {
        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = false;

            messageRetentionService.scheduleDeleteMessagesByMessageLog(expiredMessages);
        }};

        messageRetentionService.scheduleDeleteMessages(expiredMessages, mpc1);
    }

    @Test
    public void scheduleDeleteMessages_empty() {
        messageRetentionService.scheduleDeleteMessages(new ArrayList<>());

        new FullVerifications() {
        };
    }

    @Test
    public void deleteExpiredMessages() {
        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = expiredDownloadedMessagesLimit;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = expiredNotDownloadedMessagesLimit;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
            result = expiredSentMessagesLimit;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_PAYLOAD_DELETED_MAX_DELETE);
            result = expiredPayloadDeletedMessagesLimit;

        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{
            messageRetentionService.deleteExpiredMessages(mpc1, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit);
        }};
    }

    @Test
    public void batchDeleteExpiredMessages() {
        messageRetentionService.scheduleDeleteMessagesByMessageLog(expiredMessages);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, retentionMessageQueue);
            times = 2;
        }};
    }

    @Test
    public void batchDeleteExpiredMessagesSeparator() {
        List<UserMessageLogDto> batchMessages = new ArrayList<>();
        batchMessages.add(new UserMessageLogDto("", null, ""));
        batchMessages.add(new UserMessageLogDto("", null, ""));

        messageRetentionService.scheduleDeleteBatchMessages(batchMessages);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, retentionMessageQueue);
        }};
    }

    @Test
    public void batchDeleteExpiredDownloadedMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = retentionTime;

            domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
            result = ".";

            userMessageLogDao.getDownloadedUserMessagesOlderThan((Date) any, mpc1, expiredDownloadedMessagesLimit);
            result = expiredMessages;

            messageRetentionService.scheduleDeleteMessages((List<UserMessageLogDto>) any, mpc1);
            times = 1;

        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);

        new FullVerifications() {
        };
    }

    @Test
    public void batchDeleteExpiredDownloadedMessages_noMessageFound() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = retentionTime;

            domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
            result = "";

            userMessageLogDao.getDownloadedUserMessagesOlderThan((Date) any, mpc1, expiredDownloadedMessagesLimit);
            result = new ArrayList<>();

            messageRetentionService.scheduleDeleteMessages((List<UserMessageLogDto>) any, mpc1);
            times = 0;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);

        new FullVerifications() {
        };
    }

    @Test
    public void batchDeleteExpiredNotDonwloadedMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = retentionTime;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan((Date) any, mpc1, expiredNotDownloadedMessagesLimit);
            result = expiredMessages;

            messageRetentionService.scheduleDeleteMessages((List<UserMessageLogDto>) any, mpc1);
            times = 1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
    }

    @Test
    public void batchDeleteExpiredSentMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionSentByMpcURI(mpc1);
            result = retentionTime;

            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            userMessageLogDao.getSentUserMessagesOlderThan((Date) any, mpc1, expiredSentMessagesLimit, true);
            result = expiredMessages;

            messageRetentionService.scheduleDeleteMessages((List<UserMessageLogDto>) any, mpc1);
            times = 1;
        }};

        messageRetentionService.deleteExpiredSentMessages(mpc1, expiredSentMessagesLimit);

    }


    @Test
    public void shouldDeletePayloadOnSendSuccess() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
        }};

        messageRetentionService.shouldDeletePayloadOnSendSuccess();
    }

    @Test
    public void shouldDeletePayloadOnSendFailure(@Mocked UserMessage userMessage) {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
        }};

        messageRetentionService.shouldDeletePayloadOnSendFailure(userMessage);
    }


    @Test
    public void shouldDeletePayloadOnSendFailureUMFragment(@Mocked UserMessage userMessage) {

        new Expectations() {{
            userMessage.isUserMessageFragment();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            times = 0;
        }};

        Assert.assertTrue(messageRetentionService.shouldDeletePayloadOnSendFailure(userMessage));
    }

    @Test
    public void deletePayloadSendSuccess(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage);
            times = 1;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
            result = true;
        }};

        messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void deletePayloadSendFailure(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage);
            times = 1;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            result = true;

        }};

        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void notDeletePayloadSendFailure(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage);
            times = 0;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            result = false;
        }};

        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void notDeletePayloadSendSuccess(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage);
            times = 0;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
            result = false;
        }};

        messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void deleteExpiredMessagesForMpc() {

        new Expectations(messageRetentionService) {{
            //partial mocking of the following methods
            messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);
            messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
        }};

        messageRetentionService.deleteExpiredMessages(mpc1, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit);

        //the verifications are done in the Expectations block

    }

    @Test
    public void deleteExpiredDownloadedMessagesWithNegativeRetentionValue() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);

        new Verifications() {{
            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void deleteExpiredNotDownloadedMessagesWithNegativeRetentionValue() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);

        new Verifications() {{
            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void deleteExpiredPayloadDeletedMessages() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            userMessageLogDao.getDeletedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = expiredMessages;

            messageRetentionService.scheduleDeleteMessages(expiredMessages, mpc1);
            times = 1;
        }};

        messageRetentionService.deleteExpiredPayloadDeletedMessages(mpc1, messagesDeleteLimit);

        new FullVerifications() {
        };
    }

    @Test
    public void deleteExpiredPayloadDeletedMessages_noMessageFound() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            userMessageLogDao.getDeletedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = new ArrayList<>();

            messageRetentionService.scheduleDeleteMessages(expiredMessages, mpc1);
            times = 0;
        }};

        messageRetentionService.deleteExpiredPayloadDeletedMessages(mpc1, messagesDeleteLimit);

        new FullVerifications() {
        };
    }

    @Test
    public void deleteExpiredPayloadDeletedMessagesMetadataFalse() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(anyString);
            result = false;
            messageRetentionService.scheduleDeleteMessages(expiredMessages, mpc1);
            times = 0;
        }};

        messageRetentionService.deleteExpiredPayloadDeletedMessages(mpc1, messagesDeleteLimit);

        new FullVerifications() {
        };
    }

    @Test
    public void deleteExpiredDownloadedMessages() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = expiredDownloadedMessagesLimit;

            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = expiredMessages;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue);
            times = 2;
            assertEquals("Should have scheduled expiredMessages downloaded messages for deletion", expiredMessages.stream().map(message -> message.getMessageId()).collect(Collectors.toList()),
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void deleteExpiredNotDownloadedMessages() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = expiredDownloadedMessagesLimit;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = expiredMessages;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue);
            times = 2;
            assertEquals("Should have scheduled expiredMessages not downloaded messages for deletion", expiredMessages.stream().map(message -> message.getMessageId()).collect(Collectors.toList()),
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void GetRetentionValueWithValidRetentionValue() {
        final String propertyName = "retentionLimitProperty";

        new Expectations(messageRetentionService) {{
            domibusPropertyProvider.getIntegerProperty(propertyName);
            result = 5;
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName);
        Assert.assertEquals(retentionValue, Integer.valueOf(5));
    }

    @Test
    public void scheduleDeleteMessagesByMessageLog_empty() {
        messageRetentionService.scheduleDeleteMessagesByMessageLog(
                new ArrayList<>(), 1);
    }

    @Test
    public void scheduleDeleteMessagesByMessageLog_ok(@Mocked UserMessageLogDto userMessageLogDto1,
                                                      @Mocked UserMessageLogDto userMessageLogDto2) {
        List<UserMessageLogDto> batch1 = Collections.singletonList(userMessageLogDto1);
        List<UserMessageLogDto> batch2 = Collections.singletonList(userMessageLogDto2);

        new Expectations(messageRetentionService) {{
            messageRetentionService.initProperties(batch1);
            times = 1;

            messageRetentionService.scheduleDeleteBatchMessages(batch1);
            times = 1;

            messageRetentionService.initProperties(batch2);
            times = 1;

            messageRetentionService.scheduleDeleteBatchMessages(batch2);
            times = 1;
        }};

        messageRetentionService.scheduleDeleteMessagesByMessageLog(
                Arrays.asList(userMessageLogDto1, userMessageLogDto2), 1);

        new FullVerifications() {
        };
    }

    @Test
    public void initProperties(@Mocked UserMessageLogDto userMessageLogDto1,
                               @Mocked UserMessageLogDto userMessageLogDto2,
                               @Mocked UserMessage userMessage1,
                               @Mocked UserMessage userMessage2) {
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();
        new Expectations() {{
            userMessageLogDto1.getMessageId();
            result = "1";

            messagingDao.findUserMessageByMessageId("1");
            result = userMessage1;

            userMessageServiceHelper.getProperties(userMessage1);
            result = map1;

            userMessageLogDto2.getMessageId();
            result = "2";

            messagingDao.findUserMessageByMessageId("2");
            result = userMessage2;

            userMessageServiceHelper.getProperties(userMessage2);
            result = map2;
        }};

        messageRetentionService.initProperties(Arrays.asList(userMessageLogDto1, userMessageLogDto2));

        new FullVerifications() {{
            userMessageLogDto1.setProperties(map1);
            times = 1;

            userMessageLogDto2.setProperties(map2);
            times = 1;
        }};
    }
}
