package eu.domibus.ebms3.sender;

import eu.domibus.core.message.nonrepudiation.NonRepudiationCheckerImpl;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.NonRepudiationConstants;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 3.3.1
 */
@RunWith(JMockit.class)
public class NonRepudiationCheckerImplTest {

    NonRepudiationCheckerImpl nonRepudiationChecker = new NonRepudiationCheckerImpl();

    static MessageFactory messageFactory = null;

    @BeforeClass
    public static void init() throws SOAPException {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
    }

    @Test
    public void testGetNonRepudiationNodeListFromRequest() throws Exception {
        final List<String> referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request.xml");
        assertEquals(referencesFromSecurityHeader.size(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromResponse() throws Exception {
        final List<String> referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response.xml");
        assertEquals(referencesFromNonRepudiationInformation.size(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromRequestSignOnly() throws Exception {
        final List<String> referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request-signOnly.xml");
        assertEquals(referencesFromSecurityHeader.size(), 6);
    }

    @Test
    public void testGetNonRepudiationNodeListFromResponseSignOnly() throws Exception {
        final List<String> referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response-signOnly.xml");
        assertEquals(referencesFromNonRepudiationInformation.size(), 6);
    }

    @Test
    public void compareUnorderedReferenceNodeLists() throws Exception {
        final List<String> referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request.xml");
        final List<String> referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response.xml");
        final boolean compareUnorderedReferenceNodeListsResult = nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation);
        Assert.assertTrue(compareUnorderedReferenceNodeListsResult);
    }

    @Test
    public void compareUnorderedReferenceNodeListsSignOnly() throws Exception {
        final List<String> referencesFromSecurityHeader = getNonRepudiationNodeListFromRequest("dataset/as4/MSHAS4Request-signOnly.xml");
        final List<String> referencesFromNonRepudiationInformation = getNonRepudiationListFromResponse("dataset/as4/MSHAS4Response-signOnly.xml");
        final boolean compareUnorderedReferenceNodeListsResult = nonRepudiationChecker.compareUnorderedReferenceNodeLists(referencesFromSecurityHeader, referencesFromNonRepudiationInformation);
        Assert.assertTrue(compareUnorderedReferenceNodeListsResult);
    }

    protected List<String> getNonRepudiationNodeListFromRequest(String path) throws Exception {
        SOAPMessage request = new SoapUtil().createSOAPMessage(IOUtils.toString(new ClassPathResource(path).getInputStream()));
        return nonRepudiationChecker.getNonRepudiationDetailsFromSecurityInfoNode(request.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_INFO_LN).item(0));
    }

    protected List<String> getNonRepudiationListFromResponse(String path) throws Exception {
        SOAPMessage response = new SoapUtil().createSOAPMessage(IOUtils.toString(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8));
        return nonRepudiationChecker.getNonRepudiationDetailsFromReceipt(response.getSOAPHeader().getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN).item(0));
    }

}