package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.*;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class MessageRetentionServiceTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

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

    @Injectable
    private UserMessageDefaultService userMessageDefaultService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

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
    public void testscheduleDeleteMessagesDeleteMetadata() {
        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            pModeProvider.getRetentionMaxBatchByMpcURI(mpc1, domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE));
            result = maxBatch;

            messageRetentionService.deleteMessages(expiredMessages, maxBatch);
        }};

        messageRetentionService.deleteMessages(expiredMessages, mpc1);
    }

    @Test
    public void testscheduleDeleteMessages() {
        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = false;

            messageRetentionService.scheduleDeleteMessagesByMessageLog(expiredMessages);
        }};

        messageRetentionService.deleteMessages(expiredMessages, mpc1);
    }

    @Test
    public void testDeleteExpiredMessages() {
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
    public void testBatchDeleteExpiredMessages() {
        messageRetentionService.scheduleDeleteMessagesByMessageLog(expiredMessages);

        new Verifications() {{
            jmsManager.sendMessageToQueue((JmsMessage) any, retentionMessageQueue); times = 2;
        }};
    }

    @Test
    public void testBatchDeleteExpiredDownloadedMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = retentionTime;

            userMessageLogDao.getDownloadedUserMessagesOlderThan((Date)any, mpc1, expiredDownloadedMessagesLimit);
            result = expiredMessages;

            messageRetentionService.deleteMessages((List<UserMessageLogDto>)any, mpc1); times = 1;

        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);

    }

    @Test
    public void testBatchDeleteExpiredNotDonwloadedMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = retentionTime;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan((Date)any, mpc1, expiredNotDownloadedMessagesLimit);
            result = expiredMessages;

            messageRetentionService.deleteMessages((List<UserMessageLogDto>)any, mpc1); times = 1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
    }

    @Test
    public void testBatchDeleteExpiredSentMessages() {

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionSentByMpcURI(mpc1);
            result = retentionTime;

            pModeProvider.isDeleteMessageMetadataByMpcURI(mpc1);
            result = true;

            userMessageLogDao.getSentUserMessagesOlderThan((Date)any, mpc1, expiredSentMessagesLimit, true);
            result = expiredMessages;

            messageRetentionService.deleteMessages((List<UserMessageLogDto>)any, mpc1); times = 1;
        }};

        messageRetentionService.deleteExpiredSentMessages(mpc1, expiredSentMessagesLimit);

    }


    @Test
    public void testShouldDeletePayloadOnSendSuccess() {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
        }};

        messageRetentionService.shouldDeletePayloadOnSendSuccess();
    }

    @Test
    public void testShouldDeletePayloadOnSendFailure(@Mocked UserMessage userMessage) {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
        }};

        messageRetentionService.shouldDeletePayloadOnSendFailure(userMessage);
    }


    @Test
    public void testShouldDeletePayloadOnSendFailureUMFragment(@Mocked UserMessage userMessage) {

        new Expectations() {{
            userMessage.isUserMessageFragment();
            result = true;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD); times = 0;
        }};

        Assert.assertTrue(messageRetentionService.shouldDeletePayloadOnSendFailure(userMessage));
    }

    @Test
    public void testDeletePayloadSendSuccess(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage); times = 1;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
            result = true;
        }};

        messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void testDeletePayloadSendFailure(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage); times = 1;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            result = true;

        }};

        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void testNotDeletePayloadSendFailure(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage); times = 0;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            result = false;
        }};

        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void testNotDeletePayloadSendSuccess(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage); times = 0;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
            result = false;
        }};

        messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void testDeleteExpiredMessagesForMpc() {

        new Expectations(messageRetentionService) {{
            //partial mocking of the following methods
            messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);
            messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
        }};

        messageRetentionService.deleteExpiredMessages(mpc1, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit);

        //the verifications are done in the Expectations block

    }

    @Test
    public void testDeleteExpiredDownloadedMessagesWithNegativeRetentionValue() {

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
    public void testDeleteExpiredNotDownloadedMessagesWithNegativeRetentionValue() {

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
    public void testDeleteExpiredPayloadDeletedMessages() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            userMessageLogDao.getDeletedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = expiredMessages;
            pModeProvider.isDeleteMessageMetadataByMpcURI(anyString);
            result = true;
            messageRetentionService.deleteMessages(expiredMessages, mpc1); times = 1;
        }};

        messageRetentionService.deleteExpiredPayloadDeletedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
        }};
    }

    @Test
    public void testDeleteExpiredPayloadDeletedMessagesMetadataFalse() {
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.isDeleteMessageMetadataByMpcURI(anyString);
            result = false;
            messageRetentionService.deleteMessages(expiredMessages, mpc1); times = 0;
        }};

        messageRetentionService.deleteExpiredPayloadDeletedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
        }};
    }

    @Test
    public void testDeleteExpiredDownloadedMessages() {
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
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expiredMessages downloaded messages for deletion", expiredMessages.stream().map(message ->message.getMessageId()).collect(Collectors.toList()),
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessages() {
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
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expiredMessages not downloaded messages for deletion", expiredMessages.stream().map(message ->message.getMessageId()).collect(Collectors.toList()),
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void testGetRetentionValueWithValidRetentionValue() {
        final String propertyName = "retentionLimitProperty";

        new Expectations(messageRetentionService) {{
            domibusPropertyProvider.getIntegerProperty(propertyName);
            result = 5;
        }};

        final Integer retentionValue = messageRetentionService.getRetentionValue(propertyName);
        Assert.assertEquals(retentionValue, Integer.valueOf(5));
    }
}
