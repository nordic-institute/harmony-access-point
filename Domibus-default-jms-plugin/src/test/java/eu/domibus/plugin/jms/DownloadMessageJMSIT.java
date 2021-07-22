
package eu.domibus.plugin.jms;


import eu.domibus.api.model.*;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.PModeUtil;
import eu.domibus.test.UserMessageSampleUtil;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This JUNIT implements the Test cases Download Message-03 and Download Message-04.
 * It uses the JMS backend connector.
 *
 * @author martifp
 */
public class DownloadMessageJMSIT extends AbstractBackendJMSIT {

    @Autowired
    private ConnectionFactory jmsConnectionFactory;

    @Autowired
    JMSPluginImpl backendJms;

    @Autowired
    MessagingService messagingService;

    @Autowired
    UserMessageLogDefaultService userMessageLogService;

    @Autowired
    PModeUtil pModeUtil;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    UserMessageSampleUtil userMessageSampleUtil;

    @Before
    public void before() throws IOException, XmlProcessingException {
        pModeUtil.uploadPmode();
    }

    /**
     * Negative test: the message is not found in the JMS queue and a specific exception is returned.
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
     */
    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testDownloadMessageOk() throws Exception {
        String pModeKey = soapSampleUtil.composePModeKey("blue_gw", "red_gw", "testService1",
                "tc1Action", "", "pushTestcase1tc2ActionWithPayload");
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);

        String messageId = "2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu";
        final UserMessage userMessage = userMessageSampleUtil.getUserMessageTemplate();
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>";
        userMessage.setMessageId(messageId);
        ArrayList<PartInfo> partInfoList = new ArrayList<>();
        PartInfo partInfo = new PartInfo();
        partInfo.setBinaryData(messagePayload.getBytes());
        partInfo.setMime("text/xml");
        partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(messagePayload.getBytes(), "text/xml")));

        partInfoList.add(partInfo);
        messagingService.storeMessagePayloads(userMessage, partInfoList, MSHRole.RECEIVING, legConfiguration, "backendWebservice");

        UserMessageLog userMessageLog = new UserMessageLog();
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.RECEIVED);
        userMessageLog.setMessageStatus(messageStatus);
//        userMessageLog.setMessageId(messageId);
//        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.RECEIVING);
        userMessageLog.setMshRole(mshRole);
        userMessageLog.setReceived(new Date());
        userMessageLogService.save(userMessage, eu.domibus.common.MessageStatus.RECEIVED.name(), NotificationStatus.REQUIRED.name(), MSHRole.RECEIVING.name(), 1, "backendWebservice");

        javax.jms.Connection connection = jmsConnectionFactory.createConnection("domibus", "changeit");
        connection.start();

        //TODO
//        pushQueueMessage(messageId, connection, JMS_NOT_QUEUE_NAME);

        // Is this really needed since the call above is already going to eventually deliver the message through the
        // notification listener (not very clear what this test tries to achieve)
//        backendJms.deliverMessage(messageId);

        //TODO
        Message message = null;//popQueueMessageWithTimeout(connection, JMS_BACKEND_OUT_QUEUE_NAME, 5000);
        Assert.assertNotNull(message);

        connection.close();
    }


}
