package eu.domibus.plugin.ws;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Created by draguio on 17/02/2016.
 */
@DirtiesContext
@Rollback
public class SubmitMessageIT extends AbstractBackendWSIT {

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     */
    @Test
    public void testSubmitMessageValid() throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequestWs(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeaderWs(payloadHref);

        super.prepareSendMessage("validAS4Response.xml");
        SubmitResponse response = webServicePluginInterface.submitMessage(submitRequest, ebMSHeaderInfo);

        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        //message will fail as the response message does not contain the right security details(signature, etc)
        waitUntilMessageIsInWaitingForRetry(messageId);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#rsa-oaep\""))
                .withRequestBody(containing("MGF xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\" Algorithm=\"http://www.w3.org/2009/xmlenc11#mgf1sha256"))
                .withRequestBody(containing("DigestMethod xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256"))
                .withHeader("Content-Type", containing("application/soap+xml")));
    }
}
