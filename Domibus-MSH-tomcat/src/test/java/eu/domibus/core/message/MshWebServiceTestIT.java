package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3MessageInfo;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;

import static org.junit.Assert.*;

public class MshWebServiceTestIT extends AbstractIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }
    }

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    MessagingService messagingService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
    }

    @Test
    public void testGetStatusReceived() throws Exception {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);
        final Ebms3SignalMessage signalMessage = ebms3Messaging.getSignalMessage();
        assertNotNull(signalMessage);
        final Ebms3MessageInfo messageInfo = signalMessage.getMessageInfo();
        assertNotNull(messageInfo);
        assertNotNull(messageInfo.getMessageId());
        assertNotNull(signalMessage.getReceipt());
        assertEquals(1, signalMessage.getReceipt().getAny().size());
        final String receipt = signalMessage.getReceipt().getAny().get(0);
        assertTrue(receipt.contains("ebbp:NonRepudiationInformation"));
    }
}
