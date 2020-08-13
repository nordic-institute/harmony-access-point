package eu.domibus.plugin;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * // TODO reach 70% coverage.
 *
 * @author Arun Venugopal
 * @author Federico Martini
 * @since 3.3
 */
@RunWith(JMockit.class)
public class NotificationListenerServiceTest {

    private static final String TEST_FINAL_RECIPIENT = "TEST_FINAL_RECIPIENT1";
    private static final String TEST_FINAL_RECIPIENT2 = "ANOTHER_FINAL_RECIPIENT";

    @Injectable
    private Queue queue;

    @Injectable
    private BackendConnector.Mode mode;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    private JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    BackendConnectorDelegate backendConnectorDelegate;

    @Tested
    AsyncNotificationListenerService objNotificationListenerService;

    @Test
    public void testConstructor() {
        NotificationListenerService nls;
        List<NotificationType> rn = new ArrayList<>();
        rn.add(NotificationType.MESSAGE_RECEIVED);
        rn.add(NotificationType.MESSAGE_SEND_FAILURE);
        nls = new NotificationListenerService(null, BackendConnector.Mode.PUSH, rn);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().size() == 2);
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_RECEIVED));
        Assert.assertTrue(nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_FAILURE));
        Assert.assertTrue(!nls.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_SUCCESS));
    }

   /* @Test
    public void testAddToListFromQueueHappyFlow(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DefaultNotificationListenerService.PROP_LIST_PENDING_MESSAGES_MAXCOUNT);
            result = 5;

            jmsManager.browseClusterMessages((String) any, (String) any);
            result = messages;

        }};

        Collection<String> result = new ArrayList<String>();
        result.addAll(objNotificationListenerService.browseQueue(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT));
        assertEquals(5, result.size());

       *//* Assert.assertTrue(objNotificationListenerService.getRequiredNotificationTypeList().size() == 3);
        Assert.assertTrue(objNotificationListenerService.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_RECEIVED));
        Assert.assertTrue(objNotificationListenerService.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_FAILURE));
        Assert.assertTrue(objNotificationListenerService.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_RECEIVED_FAILURE));
        Assert.assertTrue(!objNotificationListenerService.getRequiredNotificationTypeList().contains(NotificationType.MESSAGE_SEND_SUCCESS));*//*

    }*/

   /* @Test
    public void testAddToListFromQueueMaxCount(final @Injectable QueueBrowser queueBrowser) throws JMSException {
        mode = BackendConnector.Mode.PULL;

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DefaultNotificationListenerService.PROP_LIST_PENDING_MESSAGES_MAXCOUNT);
            result = 5;

            jmsManager.browseClusterMessages((String) any, (String) any);
            result = messages;

        }};

        *//* Expected scenario when max pending messages configuration is not specified *//*
        Collection<String> result = new ArrayList<String>();
        result.addAll(objNotificationListenerService.browseQueue(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT));
        assertEquals(5, result.size());
    }*/
/*
    @Test
    public void testRemoveFromPendingOk() throws Exception {

        mode = BackendConnector.Mode.PULL;

        final String messageId = "ID1";
        final String queueName = "eDeliveryModule!DomibusNotifyBackendWebServiceQueue";

        final List<JmsMessage> messages = generateTestMessages();

        new Expectations() {{

            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            result = queueName;

            jmsManager.consumeMessage(queueName, messageId);
            result = messages.get(0);

        }};

        objNotificationListenerService.removeFromPending(messageId);

        new Verifications() {{
            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            jmsManager.consumeMessage(queueName, messageId);
        }};
    }*/
/*

    @Test
    public void removeFromPendingNOk() throws Exception {

        mode = BackendConnector.Mode.PULL;

        final String messageId = "ID1";
        final String queueName = "eDeliveryModule!DomibusNotifyBackendWebServiceQueue";

        new Expectations() {{

            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            result = queueName;

            jmsManager.consumeMessage(queueName, messageId);
            result = null;

        }};

        try {
            objNotificationListenerService.removeFromPending(messageId);
        } catch (MessageNotFoundException mnfEx) {
            assertEquals(mnfEx.getMessage(), "No message with id [" + messageId + "] pending for download");
        }

        new Verifications() {{
            objNotificationListenerService.getBackendNotificationQueue().getQueueName();
            jmsManager.consumeMessage(queueName, messageId);
        }};

    }
*/

    private List<JmsMessage> generateTestMessages() throws JMSException {

        List<JmsMessage> testInputList = new ArrayList<>(12);

        testInputList.add(generateTestMessage(NotificationType.MESSAGE_SEND_SUCCESS, TEST_FINAL_RECIPIENT, "ID1"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT2, "ID2"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID3"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID4"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID5"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID6"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID7"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID8"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID9"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID10"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID11"));
        testInputList.add(generateTestMessage(NotificationType.MESSAGE_RECEIVED, TEST_FINAL_RECIPIENT, "ID12"));

        return testInputList;
    }

    private JmsMessage generateTestMessage(NotificationType notificationType, String recipient, String messageid) throws JMSException {
        JmsMessage message = new JmsMessage();
        message.setProperty(MessageConstants.NOTIFICATION_TYPE, notificationType.name());
        message.setProperty((MessageConstants.FINAL_RECIPIENT), recipient);
        message.setProperty((MessageConstants.MESSAGE_ID), messageid);
        return message;
    }


   /* @Test
    public void testDoMessageStatusChange(@Injectable final Message message, @Injectable final BackendConnector backendConnector) throws Exception {
        final String messageId = "1";
        final MessageStatus fromStatus = MessageStatus.READY_TO_SEND;
        final MessageStatus toStatus = MessageStatus.ACKNOWLEDGED;
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        new Expectations() {{
            message.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            message.getStringProperty("fromStatus");
            result = fromStatus.toString();

            message.getStringProperty("toStatus");
            result = toStatus.toString();

            message.getLongProperty("changeTimestamp");
            result = timestamp.getTime();
        }};


        objNotificationListenerService.doMessageStatusChange(message);


        new Verifications() {{
            MessageStatusChangeEvent event = null;
            backendConnectorDelegate.messageStatusChanged(backendConnector, event = withCapture());
            assertEquals(event.getMessageId(), messageId);
            assertEquals(event.getFromStatus(), fromStatus);
            assertEquals(event.getToStatus(), toStatus);
            assertEquals(event.getChangeTimestamp(), timestamp);
        }};

    }
*/
   /* @Test
    public void testDoMessageReceiveFailure(@Injectable Message message,
                                            @Injectable BackendConnector backendConnector) throws JMSException {
        final String messageId = "123";
        final String errorCode = "EBMS:0001";
        final String errorDetail = "error details";
        String service = "my service";
        String serviceType = "service type";
        String action = "my action";

        new Expectations() {{
            message.getStringProperty(MessageConstants.MESSAGE_ID);
            result = messageId;

            message.getStringProperty(MessageConstants.ERROR_CODE);
            result = errorCode;

            message.getStringProperty(MessageConstants.ERROR_DETAIL);
            result = errorDetail;

            message.getStringProperty(MessageConstants.SERVICE);
            result = service;

            message.getStringProperty(MessageConstants.SERVICE_TYPE);
            result = serviceType;

            message.getStringProperty(MessageConstants.ACTION);
            result = action;
        }};

        objNotificationListenerService.doMessageReceiveFailure(message);

        new Verifications() {{
            MessageReceiveFailureEvent event = null;
            backendConnectorDelegate.messageReceiveFailed(backendConnector, event = withCapture());

            assertEquals(messageId, event.getMessageId());
            assertEquals(service, event.getService());
            assertEquals(serviceType, event.getServiceType());
            assertEquals(action, event.getAction());
        }};
    }*/
}
