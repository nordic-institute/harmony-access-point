package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.earchive.BatchEArchiveDTO;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_STORAGE_LOCATION;
import static eu.domibus.common.NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
import static eu.domibus.jms.spi.InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE;
import static java.util.Collections.singletonList;
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
    String messageId, filename;

    @Before
    public void before() throws IOException, XmlProcessingException {
        messageId = UUID.randomUUID() + "@domibus.eu";
        filename = "SOAPMessage2.xml";

        uploadPmode();

        backendConnector = new MessageReceivePluginMock("test");
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
    }

    @Test
    @Transactional
    public void testValidateAndNotifySync() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

    }

    @Test
    @Transactional
    public void testValidateAndNotifyAsync() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendFilter.getBackendName()).thenReturn(backendConnector.getName());
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendConnector()).thenReturn(backendConnector);
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendNotificationQueue()).thenReturn(notifyBackendWebServiceQueue);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

    }

    @Test(expected = WebServiceException.class)
    @Transactional
    public void testValidateAndNotifyReceivedFailure() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        filename = "InvalidBodyloadCidSOAPMessage.xml";
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
        messageId = databaseMessageHandler.submit(submission, backendConnector.getName());

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

    private File temp;
    private BatchEArchiveDTO batchEArchiveDTO;
    private String batchId;

    @Test
    @Transactional
    public void notifyMessageReceived() throws Exception {
//        batchId = UUID.randomUUID().toString();
//        temp = Files.createTempDirectory(Paths.get("target"), "tmpDirPrefix").toFile();

//        uploadPmode(SERVICE_PORT);

        String filename = "SOAPMessage4.xml";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

    }
}
