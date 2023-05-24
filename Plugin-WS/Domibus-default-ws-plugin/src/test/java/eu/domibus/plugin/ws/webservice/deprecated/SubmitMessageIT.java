package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by draguio on 17/02/2016.
 * @deprecated to be removed when deprecated endpoint /backend is removed
 */
@Deprecated
public class SubmitMessageIT extends AbstractBackendWSIT {

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     */
    @Test
    @Ignore("[EDELIVERY-8828] WSPLUGIN: tests for rest methods ignored")
    public void testSubmitMessageValid() throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref);

        super.prepareSendMessage("validAS4Response.xml", Pair.of("MESSAGE_ID", UUID.randomUUID()+"@domibus.eu"), Pair.of("REF_MESSAGE_ID", UUID.randomUUID() + "@domibus.eu"));
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);

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
