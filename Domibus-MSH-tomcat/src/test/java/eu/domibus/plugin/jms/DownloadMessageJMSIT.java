
package eu.domibus.plugin.jms;


import eu.domibus.AbstractBackendJMSIT;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.MshRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import javax.activation.DataHandler;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.Date;

/**
 * This JUNIT implements the Test cases Download Message-03 and Download Message-04.
 * It uses the JMS backend connector.
 *
 * @author martifp
 */
@DirtiesContext
@Rollback
public class DownloadMessageJMSIT extends AbstractBackendJMSIT {

    @Autowired
    private ConnectionFactory xaJmsConnectionFactory;

    @Autowired
    JMSPluginImpl backendJms;

    @Autowired
    MessagingService messagingService;

    @Autowired
    UserMessageLogDefaultService userMessageLogService;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
    }

    /**
     * Negative test: the message is not found in the JMS queue and a specific exception is returned.
     *
     * @throws RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void testDownloadMessageInvalidId() throws RuntimeException {

        // Prepare the request to the backend
        String messageId = "invalid@e-delivery.eu";

        backendJms.deliverMessage(messageId);

        Assert.fail("DownloadMessageFault was expected but was not raised");
    }

    /**
     * Tests that a message is found in the JMS queue and pushed to the business queue.
     *
     * @throws RuntimeException
     * @throws JMSException
     */
    @Test
    public void testDownloadMessageOk() throws Exception {
        String pModeKey = composePModeKey("blue_gw", "red_gw", "testService1",
                "tc1Action", "", "pushTestcase1tc2ActionWithPayload");
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        final UserMessage userMessage = getUserMessageTemplate();
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>";
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setBinaryData(messagePayload.getBytes());
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setMime("text/xml");
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(messagePayload.getBytes(), "text/xml")));
        userMessage.getMessageInfo().setMessageId(messageId);
        eu.domibus.ebms3.common.model.Messaging messaging = new eu.domibus.ebms3.common.model.Messaging();
        messaging.setUserMessage(userMessage);
        messagingService.storeMessage(messaging, MSHRole.RECEIVING, legConfiguration, "backendWebservice");

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(eu.domibus.common.MessageStatus.RECEIVED);
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setMshRole(MSHRole.RECEIVING);
        userMessageLog.setReceived(new Date());
        userMessageLogService.save(messageId, eu.domibus.common.MessageStatus.RECEIVED.name(), NotificationStatus.REQUIRED.name(), MshRole.RECEIVING.name(), 1, "default", "backendWebservice", "", null, null, null, null);

        javax.jms.Connection connection = xaJmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();
        pushQueueMessage(messageId, connection, JMS_NOT_QUEUE_NAME);

        // Is this really needed since the call above is already going to eventually deliver the message through the
        // notification listener (not very clear what this test tries to achieve)
//        backendJms.deliverMessage(messageId);

        Message message = popQueueMessageWithTimeout(connection, JMS_BACKEND_OUT_QUEUE_NAME, 5000);
        Assert.assertNotNull(message);

        connection.close();
    }


}
