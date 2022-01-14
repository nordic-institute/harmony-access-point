package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.NotificationType;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
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
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;

import static eu.domibus.common.NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
import static org.junit.Assert.assertNotNull;

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

        @Primary
        @Bean
        public BackendConnectorService backendConnectorService() {
            return Mockito.mock(BackendConnectorService.class);
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

//    @Autowired
//    BackendNotificationService backendNotificationService;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    protected BackendConnectorService backendConnectorService;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
    }

    @Test
    @Transactional
    public void testValidateAndNotify_propertyNull() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class))).thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

//        String backendName = "backendName";
//        NotificationType notificationType = NotificationType.MESSAGE_RECEIVED;

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

//        backendNotificationService.notifyMessageReceived(userMessage, backendName, notificationType, null);

    }
}
