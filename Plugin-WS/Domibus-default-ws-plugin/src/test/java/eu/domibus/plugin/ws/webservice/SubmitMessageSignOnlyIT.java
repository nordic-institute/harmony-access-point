package eu.domibus.plugin.ws.webservice;

import eu.domibus.core.message.nonrepudiation.NonRepudiationChecker;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by draguio on 17/02/2016.
 */
public class SubmitMessageSignOnlyIT extends AbstractBackendWSIT {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(WebServiceImpl.class);

    @Autowired
    NonRepudiationChecker nonRepudiationChecker;

    @Autowired
    MessageRetentionDefaultService messageRetentionDefaultService;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Test for the backend sendMessage service with payload profile enabled
     */
    @Test
    public void testSubmitMessageValid() throws SubmitMessageFault {
        String msgId = UUID.randomUUID() + "@domibus.eu";
        String refMsgId = UUID.randomUUID() + "@domibus.eu";
        LOG.info("Message Id: [{}] RefMsgId: [{}]", msgId, refMsgId);
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequestWs(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeaderWs(payloadHref);
        ebMSHeaderInfo.getUserMessage().getCollaborationInfo().setAction("TC4Leg1");

        super.prepareSendMessage("validAS4Response.xml",
                Pair.of("MESSAGE_ID", msgId),
                Pair.of("REF_MESSAGE_ID", refMsgId));
        SubmitResponse response = webServicePluginInterface.submitMessage(submitRequest, ebMSHeaderInfo);

        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        //message will fail as the response message does not contain the right security details(signature, etc)
        waitUntilMessageIsInWaitingForRetry(messageId);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(containing("DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\""))
                .withHeader("Content-Type", notMatching("application/soap+xml")));

        messageRetentionDefaultService.deleteAllMessages();
    }
}
