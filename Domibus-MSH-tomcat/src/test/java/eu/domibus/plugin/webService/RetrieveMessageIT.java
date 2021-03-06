package eu.domibus.plugin.webService;

import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.MSHRole;
import eu.domibus.common.NotificationType;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.plugin.notification.NotifyMessageCreator;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;

@DirtiesContext
@Rollback
public class RetrieveMessageIT extends AbstractBackendWSIT {

    @Autowired
    JMSManager jmsManager;

    @Autowired
    MessagingService messagingService;

    @Autowired
    protected MessageRetriever messageRetriever;

    @Autowired
    UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected ConfigurationDAO configurationDAO;


    @Before
    public void updatePMode() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    @DirtiesContext
    @Test(expected = RetrieveMessageFault.class)
    public void testMessageIdEmpty() throws RetrieveMessageFault {
        retrieveMessageFail("", "Message ID is empty");
    }

    @DirtiesContext
    @Test(expected = RetrieveMessageFault.class)
    public void testMessageNotFound() throws RetrieveMessageFault {
        retrieveMessageFail("notFound", "Message not found, id [notFound]");
    }

    @DirtiesContext
    @Test
    public void testMessageIdNeedsATrimSpaces() throws Exception {
        retrieveMessage("    2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu ");
    }

    @DirtiesContext
    @Test
    public void testMessageIdNeedsATrimTabs() throws Exception {
        retrieveMessage("\t2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu\t");
    }

    @DirtiesContext
    @Test
    public void testMessageIdNeedsATrimSpacesAndTabs() throws Exception {
        retrieveMessage(" \t 2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu \t ");
    }

    @DirtiesContext
    @Test
    public void testRetrieveMessageOk() throws Exception {
        retrieveMessage("2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu");
    }

    private void retrieveMessageFail(String messageId, String errorMessage) throws RetrieveMessageFault {
        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);

        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            backendWebService.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (RetrieveMessageFault re) {
            Assert.assertEquals(errorMessage, re.getMessage());
            throw re;
        }
        Assert.fail("DownloadMessageFault was expected but was not raised");
    }

    private void retrieveMessage(String messageId) throws Exception {
        String pModeKey = composePModeKey("blue_gw", "red_gw", "testService1",
                "tc1Action", "", "pushTestcase1tc2ActionWithPayload");
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);


        final String sanitazedMessageId = StringUtils.trim(messageId).replace("\t", "");
        final UserMessage userMessage = getUserMessageTemplate();
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<hello>world</hello>";
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setBinaryData(messagePayload.getBytes());
        userMessage.getPayloadInfo().getPartInfo().iterator().next().setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(messagePayload.getBytes(), "text/xml")));
        userMessage.getMessageInfo().setMessageId(sanitazedMessageId);
        eu.domibus.ebms3.common.model.Messaging messaging = new eu.domibus.ebms3.common.model.Messaging();
        messaging.setUserMessage(userMessage);
        messagingService.storeMessage(messaging, MSHRole.RECEIVING, legConfiguration, "backendWebservice");

        UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageStatus(eu.domibus.common.MessageStatus.RECEIVED);
        userMessageLog.setMessageId(sanitazedMessageId);
        userMessageLog.setMessageType(MessageType.USER_MESSAGE);
        userMessageLog.setMshRole(MSHRole.RECEIVING);
        userMessageLog.setReceived(new Date());
        userMessageLogService.save(sanitazedMessageId,
                eu.domibus.common.MessageStatus.RECEIVED.name(),
                NotificationStatus.REQUIRED.name(),
                MshRole.RECEIVING.name(),
                1,
                "default",
                "backendWebservice",
                "",
                null,
                null,
                null,
                null);

        final JmsMessage jmsMessage = new NotifyMessageCreator(sanitazedMessageId, NotificationType.MESSAGE_RECEIVED, new HashMap<>()).createMessage();
        jmsManager.sendMessageToQueue(jmsMessage, WS_NOT_QUEUE);
        // requires a time to consume messages from the notification queue
        waitForMessage(messageId);
        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);
        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            backendWebService.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (RetrieveMessageFault dmf) {
            String message = "Downloading message failed";
            Assert.assertEquals(message, dmf.getMessage());
            throw dmf;
        }
        Assert.assertFalse(retrieveMessageResponse.value.getPayload().isEmpty());
        LargePayloadType payloadType = retrieveMessageResponse.value.getPayload().iterator().next();
        String payload = IOUtils.toString(payloadType.getValue().getDataSource().getInputStream(), Charset.defaultCharset());
        Assert.assertEquals(payload, messagePayload);
    }

    private RetrieveMessageRequest createRetrieveMessageRequest(String messageId) {
        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        retrieveMessageRequest.setMessageID(messageId);
        return retrieveMessageRequest;
    }
}
