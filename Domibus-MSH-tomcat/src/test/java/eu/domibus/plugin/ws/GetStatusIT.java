package eu.domibus.plugin.ws;

import eu.domibus.AbstractBackendWSIT;
import eu.domibus.core.message.MessagingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.StatusFault;
import eu.domibus.plugin.ws.generated.body.MessageStatus;
import eu.domibus.plugin.ws.generated.body.StatusRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;

@DirtiesContext
@Rollback
public class GetStatusIT extends AbstractBackendWSIT {

    @Autowired
    MessagingService messagingService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    @Test
    public void testGetStatusReceived() throws StatusFault, IOException, SOAPException, SAXException, ParserConfigurationException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        StatusRequest messageStatusRequest = createMessageStatusRequest(messageId);
        MessageStatus response = webServicePluginInterface.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.RECEIVED, response);
    }

    @Test
    public void testGetStatusInvalidId() throws StatusFault {
        String invalidMessageId = "invalid";
        StatusRequest messageStatusRequest = createMessageStatusRequest(invalidMessageId);
        MessageStatus response = webServicePluginInterface.getStatus(messageStatusRequest);
        Assert.assertEquals(MessageStatus.NOT_FOUND, response);
    }

    @Test
    public void testGetStatusEmptyMessageId() {
        String emptyMessageId = "";
        StatusRequest messageStatusRequest = createMessageStatusRequest(emptyMessageId);
        try {
            webServicePluginInterface.getStatus(messageStatusRequest);
            Assert.fail();
        } catch (StatusFault statusFault) {
            String message = "Message ID is empty";
            Assert.assertEquals(message, statusFault.getMessage());
        }
    }

    private StatusRequest createMessageStatusRequest(final String messageId) {
        StatusRequest statusRequest = new StatusRequest();
        statusRequest.setMessageID(messageId);
        return statusRequest;
    }
}
