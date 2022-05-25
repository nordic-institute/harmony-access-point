package eu.domibus.plugin.ws.webservice;

import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.UUID;

/**
 * @author venugar
 * @since 3.3
 */
public class SubmitMessageCaseInsensitiveIT extends AbstractBackendWSIT {

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Before
    public void updatePMode() throws IOException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
    }

    /**
     * Sample example of a test for the backend sendMessage service.
     * The message components should be case insensitive from the PMode data
     *
     */
    @Ignore("EDELIVERY-8892")
    @Test
    public void testSubmitMessageOK() throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequestWs(payloadHref);

        Pair<String, String> message_id = Pair.of("MESSAGE_ID", UUID.randomUUID() + "@domibus.eu");
        super.prepareSendMessage("validAS4Response.xml", message_id, Pair.of("REF_MESSAGE_ID", UUID.randomUUID() + "@domibus.eu"));

        final eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging messaging = createMessageHeaderWs(payloadHref);
        messaging.getUserMessage().getCollaborationInfo().setAction("TC3Leg1");

        SubmitResponse response = webServicePluginInterface.submitMessage(submitRequest, messaging);
        verifySendMessageAck(response);

        messageRetentionService.deleteAllMessages();
    }
}
