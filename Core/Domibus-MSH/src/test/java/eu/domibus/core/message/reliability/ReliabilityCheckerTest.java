package eu.domibus.core.message.reliability;

import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.nonrepudiation.NonRepudiationConstants;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import static org.junit.Assert.assertEquals;


/**
 * @author Joze Rihtarsic
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ReliabilityCheckerTest {


    ReliabilityChecker testInstance = new ReliabilityChecker();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getNonRepudiationDetailsNodeFromReceiptEmpty(@Mocked SOAPMessage response,
                                                             @Mocked SOAPHeader header,
                                                             @Mocked NodeList nodelist) throws SOAPException, EbMS3Exception {
        String messageId = "TestMessageId";
        new Expectations() {{
            response.getSOAPHeader();
            result = header;
            header.getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN);
            result = nodelist;
            nodelist.getLength();
            result = 0;
        }};

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Invalid NonRepudiationInformation: No element found");

        testInstance.getNonRepudiationDetailsNodeFromReceipt(response, messageId);
    }

    @Test
    public void getNonRepudiationDetailsNodeFromReceiptNull(@Mocked SOAPMessage response,
                                                            @Mocked SOAPHeader header,
                                                            @Mocked NodeList nodelist) throws SOAPException, EbMS3Exception {
        String messageId = "TestMessageId";
        new Expectations() {{
            response.getSOAPHeader();
            result = header;
            header.getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN);
            result = nodelist;
            nodelist.getLength();
            result = 1;
            nodelist.item(0);
            result = null;
        }};

        thrown.expect(EbMS3Exception.class);
        thrown.expectMessage("Invalid NonRepudiationInformation: No element found");

        testInstance.getNonRepudiationDetailsNodeFromReceipt(response, messageId);
    }

    @Test
    public void getNonRepudiationDetailsNodeFromReceiptExists(@Mocked SOAPMessage response,
                                                              @Mocked SOAPHeader header,
                                                              @Mocked NodeList nodelist,
                                                              @Mocked Node node) throws SOAPException, EbMS3Exception {

        String messageId = "TestMessageId";
        new Expectations() {{
            response.getSOAPHeader();
            result = header;
            header.getElementsByTagNameNS(NonRepudiationConstants.NS_NRR, NonRepudiationConstants.NRR_LN);
            result = nodelist;
            nodelist.getLength();
            result = 1;
            nodelist.item(0);
            result = node;
        }};
        Node result = testInstance.getNonRepudiationDetailsNodeFromReceipt(response, messageId);
        assertEquals(node, result);
    }
}