package eu.domibus.plugin.ws.webservice;

import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


/**
 * This class implements the test cases Receive Message-01 and Receive Message-02.
 *
 * @author draguio
 * @author martifp
 */
public class ReceiveMessageIT extends AbstractBackendWSIT {

    @Autowired
    MSHWebservice mshWebserviceTest;
    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    MessageRetentionDefaultService messageRetentionDefaultService;

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
    @Test
    public void testReceiveMessage() throws SOAPException, IOException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage2.xml";
        String messageId = Thread.currentThread().getId()+""+ UUID.randomUUID() + "@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        messageRetentionDefaultService.deleteAllMessages();
    }

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
