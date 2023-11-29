package eu.domibus.plugin.ws.webservice;

import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginDispatchClientProvider;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.UUID;


/**
 * This class implements the test cases Receive Message-01 and Receive Message-02.
 *
 * @author draguio
 * @author martifp
 */
public class ReceiveMessageIT extends AbstractBackendWSIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public WSPluginDispatchClientProvider wsPluginDispatchClientProvider() {
            return Mockito.mock(WSPluginDispatchClientProvider.class);
        }
    }

    @Autowired
    MSHWebservice mshWebserviceTest;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    MessageRetentionDefaultService messageRetentionDefaultService;

    @Autowired
    WSPluginDispatchClientProvider wsPluginDispatchClientProvider;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * This test invokes the MSHWebService and verifies that the message is stored
     * in the database with the status RECEIVED
     *
     * @throws SOAPException, IOException, SQLException, ParserConfigurationException, SAXException
     *                        <p>
     *                        ref: Receive Message-01
     */
    @Ignore //TODO: will be fixed by EDELIVERY-11139
    @Test
    public void testReceiveMessage() throws SOAPException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        String filename = "SOAPMessage2.xml";
        String messageId = UUID.randomUUID() + "@domibus.eu";

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        messageRetentionDefaultService.deleteAllMessages();
    }

    @Ignore //TODO: will be fixed by EDELIVERY-11139
    @Test
    public void testDeleteBatch() throws SOAPException, IOException, ParserConfigurationException, SAXException, InterruptedException {
        String filename = "SOAPMessage2.xml";
        String messageId = UUID.randomUUID() + "@domibus.eu";

        Dispatch dispatch = Mockito.mock(Dispatch.class);
        SOAPMessage reply = Mockito.mock(SOAPMessage.class);
        Mockito.when(dispatch.invoke(Mockito.any(SOAPMessage.class)))
                .thenReturn(reply);
        Mockito.when(wsPluginDispatchClientProvider.getClient(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(dispatch);

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        messageRetentionDefaultService.deleteAllMessages();

        Thread.sleep(1000);

    }

    @Ignore //TODO: will be fixed by EDELIVERY-11139
    @Test
    public void testReceiveTestMessage() throws Exception {
        String filename = "SOAPTestMessage.xml";
        String messageId = "ping123@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);

        mshWebserviceTest.invoke(soapMessage);
        waitUntilMessageIsReceived(messageId);

        messageRetentionDefaultService.deleteAllMessages();
    }

}
