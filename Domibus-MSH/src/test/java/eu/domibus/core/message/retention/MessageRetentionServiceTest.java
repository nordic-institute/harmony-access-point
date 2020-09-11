package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageFragmentEntity;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import javax.validation.constraints.AssertTrue;
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

    @Tested
    MessageRetentionDefaultService messageRetentionService;

    @Test
    public void testDeleteExpiredMessages() {
        final String mpc1 = "mpc1";
        final String mpc2 = "mpc2";
        final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = 10;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = 20;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
            result = 30;

        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{
            messageRetentionService.deleteExpiredMessages(mpc1, 10, 20, 30);
        }};
    }


    @Test
    public void testBatchDeleteExpiredMessages() {
        final String mpc1 = "mpc1";
        final String mpc2 = "mpc2";
        final int maxBatch = 1000;
        final int retentionTime = 60;
        final List<String> mpcs = Arrays.asList(new String[]{mpc1, mpc2});

        List<String> expired = Arrays.asList(new String[]{"abc", "def", "inva,lid"});

        new Expectations(messageRetentionService) {{
            pModeProvider.getMpcURIList();
            result = mpcs;

            pModeProvider.isDeleteMessageMetadataByMpcURI(anyString);
            result = true;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
            result = 10;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
            result = 20;

            messageRetentionService.getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
            result = 30;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE);
            result = maxBatch;

            pModeProvider.getRetentionMaxBatchByMpcURI(mpc1, maxBatch);
            result = maxBatch;

            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = retentionTime;

            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = retentionTime;

            pModeProvider.getRetentionSentByMpcURI(mpc1);
            result = retentionTime;

            userMessageLogDao.getDownloadedUserMessagesOlderThan((Date)any, mpc1, 10);
            result = expired;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan((Date)any, mpc1, 20);
            result = expired;

            userMessageLogDao.getSentUserMessagesOlderThan((Date)any, mpc1, 30);
            result = expired;

            domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR);
            result = ",";

        }};

        messageRetentionService.deleteExpiredMessages();

        new Verifications() {{
            messageRetentionService.deleteExpiredMessages(mpc1, 10, 20, 30);
            messageRetentionService.scheduleDeleteMessages((List<String>)any, maxBatch); times = 3;
            messageRetentionService.scheduleDeleteMessages((List<String>)any); times = 3;
        }};
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
    public void testNotDeletePayload(@Mocked UserMessage userMessage, @Mocked UserMessageLog userMessageLog) {

        new Expectations() {{
            //partial mocking of the following methods
            messagingDao.clearPayloadData(userMessage); times = 0;
            messageRetentionService.deletePayload(userMessage, userMessageLog);

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
            result = false;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
            result = false;
        }};

        messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);
        messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);

        //the verifications are done in the Expectations block
    }

    @Test
    public void testDeleteExpiredMessagesForMpc() {
        final String mpc1 = "mpc1";
        final Integer expiredDownloadedMessagesLimit = 10;
        final Integer expiredNotDownloadedMessagesLimit = 20;

        new Expectations(messageRetentionService) {{
            //partial mocking of the following methods
            messageRetentionService.deleteExpiredDownloadedMessages(mpc1, expiredDownloadedMessagesLimit);
            messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, expiredNotDownloadedMessagesLimit);
        }};

        messageRetentionService.deleteExpiredMessages(mpc1, 10, 20, 30);

        //the verifications are done in the Expectations block

    }

    @Test
    public void testDeleteExpiredDownloadedMessagesWithNegativeRetentionValue() {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessagesWithNegativeRetentionValue() {
        final String mpc1 = "mpc1";

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = -1;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, 10);

        new Verifications() {{
            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), anyString, null);
            times = 0;
        }};
    }

    @Test
    public void testDeleteExpiredDownloadedMessages() {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionDownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getDownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expired downloaded messages for deletion", downloadedMessageIds,
                    jmsMessages.stream().map(jmsMessage -> jmsMessage.getStringProperty(MessageConstants.MESSAGE_ID)).collect(Collectors.toList()));
        }};
    }

    @Test
    public void testDeleteExpiredNotDownloadedMessages() {
        String id1 = "1";
        String id2 = "2";
        final List<String> downloadedMessageIds = Arrays.asList(new String[]{id1, id2});
        final String mpc1 = "mpc1";
        final Integer messagesDeleteLimit = 5;

        new Expectations(messageRetentionService) {{
            pModeProvider.getRetentionUndownloadedByMpcURI(mpc1);
            result = 10;

            userMessageLogDao.getUndownloadedUserMessagesOlderThan(withAny(new Date()), mpc1, null);
            result = downloadedMessageIds;
        }};

        messageRetentionService.deleteExpiredNotDownloadedMessages(mpc1, messagesDeleteLimit);

        new Verifications() {{
            List<JmsMessage> jmsMessages = new ArrayList<>();
            jmsManager.sendMessageToQueue(withCapture(jmsMessages), retentionMessageQueue); times = 2;
            assertEquals("Should have scheduled expired not downloaded messages for deletion", downloadedMessageIds,
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
