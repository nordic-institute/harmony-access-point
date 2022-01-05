package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.AbstractIT;
import eu.domibus.core.ebms3.receiver.leg.MessageLegConfigurationFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;
import java.util.UUID;


/**
 * @author draguio
 * @since 3.3
 */
@DirtiesContext
@Rollback
public class SetPolicyInInterceptorIT extends AbstractIT {

    @Autowired
    SoapSampleUtil soapSampleUtil;
    @Autowired
    SetPolicyInServerInterceptor setPolicyInInterceptorServer;

    @Autowired
    MessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(SERVICE_PORT);
    }

    @Test
    public void testHandleMessage() throws  IOException {
        String expectedPolicy = "eDeliveryAS4Policy";
        String expectedSecurityAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

        String filename = "SOAPMessage2.xml";

        SoapMessage sm = createSoapMessage(filename, UUID.randomUUID() + "@domibus.eu");

        setPolicyInInterceptorServer.handleMessage(sm);

        Assert.assertEquals(expectedPolicy, ((Policy) sm.get(PolicyConstants.POLICY_OVERRIDE)).getId());
        Assert.assertEquals(expectedSecurityAlgorithm, sm.get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM));
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testHandleMessageNull() throws IOException {

        String filename = "SOAPMessageNoMessaging.xml";
        SoapMessage sm = createSoapMessage(filename, UUID.randomUUID() + "@domibus.eu");

        // handle message without adding any content
        setPolicyInInterceptorServer.handleMessage(sm);
    }



    @Test
    public void testHandleGetVerb() throws IOException {
        HttpServletResponse response = new MockHttpServletResponse();
        String filename = "SOAPMessageNoMessaging.xml";
        SoapMessage sm = createSoapMessage(filename, UUID.randomUUID() + "@domibus.eu");
        sm.put("org.apache.cxf.request.method", "GET");
        sm.put(AbstractHTTPDestination.HTTP_RESPONSE, response);

        // handle message without adding any content
        setPolicyInInterceptorServer.handleMessage(sm);

        try {
            String reply = ((MockHttpServletResponse) sm.get(AbstractHTTPDestination.HTTP_RESPONSE)).getContentAsString();

            Assert.assertTrue(reply.contains("domibus-MSH"));

        } catch (UnsupportedEncodingException e) {
            Assert.fail();
        }
    }

    public SoapMessage createSoapMessage(String filename, String messageId) throws IOException {
        String datasetString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dataset/as4/" + filename), StandardCharsets.UTF_8);
        datasetString = StringUtils.replace(datasetString, "MESSAGE_ID", messageId);
        Reader reader = new StringReader(datasetString);
        XMLStreamReader xmlReader = null;
        XMLInputFactory factory = XMLInputFactory.newInstance(); // Or newFactory()
        try {
            xmlReader = factory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new IllegalArgumentException("Could not create XML", e);
        }

        SoapMessage soapMessage = new SoapMessage(Soap11.getInstance());
        soapMessage.setContent(XMLStreamReader.class, xmlReader);
        soapMessage.setInterceptorChain(new PhaseInterceptorChain(new TreeSet<>()));
        soapMessage.setExchange(new ExchangeImpl());
        return soapMessage;
    }
}
