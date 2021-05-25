package eu.domibus.plugin;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.common.MessageDBUtil;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 5.0
 */
//@DirtiesContext
//@Rollback
@Ignore
public abstract class DeleteMessageIT extends AbstractBackendWSIT {

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    @Autowired
    MessageDBUtil messageDBUtil;

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    protected static List<String> tablesToExclude;

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }
    }

    @BeforeClass
    public static void initTablesToExclude() {
        tablesToExclude = new ArrayList<>(Arrays.asList(
                "TB_EVENT",
                "TB_EVENT_ALERT",
                "TB_EVENT_PROPERTY",
                "TB_ALERT"
        ));
    }

    protected void receiveMessageToDelete() throws SOAPException, IOException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
    }

    protected void deleteMessages() {
        messageRetentionService.deleteExpiredMessages();
    }

    protected void sendMessageToDelete(MessageStatus status) throws SubmitMessageFault {
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

        waitUntilMessageHasStatus(messageId, status);

        verify(postRequestedFor(urlMatching("/domibus/services/msh")));

    }
}