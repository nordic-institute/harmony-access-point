package eu.domibus.plugin.ws.webservice;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.*;
import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.plugin.notification.NotifyMessageCreator;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.body.LargePayloadType;
import eu.domibus.plugin.ws.generated.body.MshRole;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageRequest;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.test.common.UserMessageSampleUtil;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Holder;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RetrieveMessageIT extends AbstractBackendWSIT {

    @Autowired
    JMSManager jmsManager;

    @Autowired
    MessagingService messagingService;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    protected MessageRetriever messageRetriever;

    @Autowired
    UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected ConfigurationDAO configurationDAO;

    @Autowired
    MSHWebservice mshWebserviceTest;

    @Autowired
    WSPluginImpl wsPlugin;

    @Autowired
    MessageRetentionDefaultService messageRetentionDefaultService;

    @Before
    public void updatePMode() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    @Test(expected = RetrieveMessageFault.class)
    public void testMessageIdEmpty() throws RetrieveMessageFault {
        retrieveMessageFail("", "Message ID is empty");
    }

    @Test(expected = RetrieveMessageFault.class)
    public void testMessageNotFound() throws RetrieveMessageFault {
        retrieveMessageFail("notFound", "Message not found, id [notFound]");
    }

    @Test
    public void testRetrieveMessageOk() throws Exception {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitForMessage(messageId);

        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);
        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            webServicePluginInterface.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (RetrieveMessageFault dmf) {
            String message = "Downloading message failed";
            assertEquals(message, dmf.getMessage());
            throw dmf;
        }

        final Messaging messaging = ebMSHeaderInfo.value;
        final UserMessage userMessage = messaging.getUserMessage();
        assertEquals(messageId, userMessage.getMessageInfo().getMessageId());
        assertEquals(userMessage.getMessageProperties().getProperty().size(), 2);

        messageRetentionDefaultService.deleteAllMessages();
    }

    private void retrieveMessageFail(String messageId, String errorMessage) throws RetrieveMessageFault {
        RetrieveMessageRequest retrieveMessageRequest = createRetrieveMessageRequest(messageId);

        Holder<RetrieveMessageResponse> retrieveMessageResponse = new Holder<>();
        Holder<eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging> ebMSHeaderInfo = new Holder<>();

        try {
            webServicePluginInterface.retrieveMessage(retrieveMessageRequest, retrieveMessageResponse, ebMSHeaderInfo);
        } catch (RetrieveMessageFault re) {
            assertEquals(errorMessage, re.getMessage());
            throw re;
        }
        Assert.fail("DownloadMessageFault was expected but was not raised");
    }


    private RetrieveMessageRequest createRetrieveMessageRequest(String messageId) {
        RetrieveMessageRequest retrieveMessageRequest = new RetrieveMessageRequest();
        retrieveMessageRequest.setMessageID(messageId);
        return retrieveMessageRequest;
    }
}
