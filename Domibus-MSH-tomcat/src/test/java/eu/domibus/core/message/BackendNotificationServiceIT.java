package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.test.common.MessageReceivePluginMock;
import eu.domibus.test.common.SoapSampleUtil;
import eu.domibus.test.common.SubmissionUtil;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.jms.Queue;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static eu.domibus.common.NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
import static eu.domibus.jms.spi.InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE;
import static org.junit.Assert.assertEquals;
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

        @Primary
        @Bean
        PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration() {
            return Mockito.mock(PluginAsyncNotificationConfiguration.class);
        }

        @Bean("notifyBackendWebServiceQueue")
        public ActiveMQQueue notifyBackendWSQueue() {
            return new ActiveMQQueue("domibus.notification.webservice");
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

    @Autowired
    PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration;

    @Autowired
    Queue notifyBackendWebServiceQueue;

    @Autowired
    @Qualifier(UNKNOWN_RECEIVER_QUEUE)
    protected Queue unknownReceiverQueue;

    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    MessageReceivePluginMock backendConnector;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();

        backendConnector = new MessageReceivePluginMock("test");
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
    }

    @Test
    @Transactional
    public void testValidateAndNotifySync() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
//        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
//        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

    }

    @Test
    @Transactional
    public void testValidateAndNotifyAsync() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
        String backendName = "domibusBackend";

//        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
//        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
//        Mockito.when(backendConnector.getName()).thenReturn(backendName);

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendFilter.getBackendName()).thenReturn(backendName);
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendConnector()).thenReturn(backendConnector);
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendNotificationQueue()).thenReturn(notifyBackendWebServiceQueue);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

    }

    @Test(expected = WebServiceException.class)
    @Transactional
    public void testValidateAndNotifyReceivedFailure() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
//        MessageReceivePluginMock backendConnector = new MessageReceivePluginMock("test");
//        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        String filename = "InvalidBodyloadCidSOAPMessage.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

        assertEquals(backendConnector.getMessageReceiveFailureEvent().getMessageId(), messageId);
    }

    @Test
    @Transactional
    public void notifyPayloadEvent() throws MessagingProcessingException {
        Submission submission = submissionUtil.createSubmission();
        final String messageId = databaseMessageHandler.submit(submission, backendConnector.getName());

        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        assertNotNull(userMessageLog);
        assertEquals(backendConnector.getPayloadSubmittedEvent().getMessageId(), messageId);
        assertEquals(backendConnector.getPayloadProcessedEvent().getMessageId(), messageId);

        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        MessageLogResultRO result =messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);
        assertNotNull(result);
        assertEquals(result.getMessageLogEntries().size(), 1);
        assertEquals(result.getMessageLogEntries().get(0).getMessageId(), messageId);
    }

}
