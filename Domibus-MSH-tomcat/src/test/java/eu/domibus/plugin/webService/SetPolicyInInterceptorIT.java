package eu.domibus.plugin.webService;

import eu.domibus.AbstractBackendWSIT;
import eu.domibus.core.ebms3.receiver.leg.MessageLegConfigurationFactory;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInServerInterceptor;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.cxf.binding.soap.SoapMessage;
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
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author draguio
 * @since 3.3
 */
@DirtiesContext
@Rollback
public class SetPolicyInInterceptorIT extends AbstractBackendWSIT {

    @Autowired
    SetPolicyInServerInterceptor setPolicyInInterceptorServer;

    @Autowired
    MessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
//        setPolicyInInterceptorServer.setMessageLegConfigurationFactory(serverInMessageLegConfigurationFactory);
    }

    @Test
    public void testHandleMessage() {
        String expectedPolicy = "eDeliveryAS4Policy";
        String expectedSecurityAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

        String filename = "SOAPMessage2.xml";
        SoapMessage sm = createSoapMessage(filename);

        setPolicyInInterceptorServer.handleMessage(sm);

        Assert.assertEquals(expectedPolicy, ((Policy) sm.get(PolicyConstants.POLICY_OVERRIDE)).getId());
        Assert.assertEquals(expectedSecurityAlgorithm, sm.get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM));
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testHandleMessageNull() {

        String filename = "SOAPMessageNoMessaging.xml";
        SoapMessage sm = createSoapMessage(filename);

        // handle message without adding any content
        setPolicyInInterceptorServer.handleMessage(sm);
    }

    @Test
    public void testHandleGetVerb() {
        HttpServletResponse response = new MockHttpServletResponse();
        String filename = "SOAPMessageNoMessaging.xml";
        SoapMessage sm = createSoapMessage(filename);
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
}
