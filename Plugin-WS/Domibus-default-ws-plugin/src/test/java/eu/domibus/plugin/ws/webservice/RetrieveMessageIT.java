package eu.domibus.plugin.ws.webservice;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageRequest;
import eu.domibus.plugin.ws.generated.body.RetrieveMessageResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Holder;
import java.io.IOException;

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

    @Ignore //TODO: will be fixed by EDELIVERY-11139
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
            Assert.assertTrue(dmf.getMessage().contains(WebServiceImpl.MESSAGE_NOT_FOUND_ID));
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
