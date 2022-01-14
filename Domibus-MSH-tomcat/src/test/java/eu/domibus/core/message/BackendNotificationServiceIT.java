package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.NotificationType;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.test.common.SoapSampleUtil;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;

public class BackendNotificationServiceIT extends AbstractIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorProvider backendConnectorProvider() {
            return Mockito.mock(BackendConnectorProvider.class);
        }

        @Primary
        @Bean
        public RoutingService routingService() {
            return Mockito.mock(RoutingService.class);
        }
    }

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    RoutingService routingService;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    MSHWebservice mshWebserviceTest;

    @Autowired
    BackendNotificationService backendNotificationService;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
    }

    @Test
    @Transactional
    public void testValidateAndNotify_propertyNull() throws SOAPException, IOException, ParserConfigurationException, SAXException {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        String backendName = "backendName";
        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
//        backendNotificationService.notifyMessageReceived(userMessage, backendName, notificationType, null);

    }
}
