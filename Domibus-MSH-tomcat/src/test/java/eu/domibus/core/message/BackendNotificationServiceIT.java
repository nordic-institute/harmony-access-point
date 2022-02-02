package eu.domibus.core.message;

import eu.domibus.ITTestsService;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.ebms3.sender.MessageSenderErrorHandler;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.dictionary.NotificationStatusDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.test.common.BackendConnectorMock;
import eu.domibus.test.common.SoapSampleUtil;
import eu.domibus.test.common.SubmissionUtil;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.neethi.Policy;
import org.junit.After;
import org.junit.Assert;
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
import javax.persistence.NoResultException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING;
import static eu.domibus.common.NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
import static eu.domibus.jms.spi.InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BackendNotificationServiceIT extends DeleteMessageAbstractIT {

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

        @Primary
        @Bean("notifyBackendWebServiceQueue")
        public ActiveMQQueue notifyBackendWSQueue() {
            return new ActiveMQQueue("domibus.notification.webservice");
        }

        @Primary
        @Bean
        MSHDispatcher mshDispatcher() {
            return Mockito.mock(MSHDispatcher.class);
        }

        @Primary
        @Bean
        ResponseHandler responseHandler() {
            return Mockito.mock(ResponseHandler.class);
        }

        @Primary
        @Bean
        ReliabilityChecker reliabilityChecker() {
            return Mockito.mock(ReliabilityChecker.class);
        }
    }

    @Autowired
    protected MessageExchangeService messageExchangeService;

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

    @Autowired
    private ITTestsService itTestsService;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Autowired
    protected ResponseHandler responseHandler;

    @Autowired
    protected ReliabilityChecker reliabilityChecker;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    NotificationStatusDao notificationStatusDao;

    @Autowired
    @Qualifier("messageSenderErrorHandler")
    protected MessageSenderErrorHandler messageSenderErrorHandler;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendNotificationServiceIT.class);

    BackendConnectorMock backendConnector;
    String messageId, filename;

    @Before
    public void before() throws IOException, XmlProcessingException {
        messageId = UUID.randomUUID() + "@domibus.eu";
        filename = "SOAPMessage2.xml";

        uploadPmode();

        backendConnector = new BackendConnectorMock("domibusBackend");
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class)))
                .thenReturn(backendConnector);
    }

    @After
    public void after() {
        backendConnector.clear();
        List<MessageLogInfo> list = userMessageLogDao.findAllInfoPaged(0, 100, "ID_PK", true, new HashMap<>());
        if (list.size() > 0) {
            list.forEach(el -> {
                UserMessageLog res = userMessageLogDao.findByMessageId(el.getMessageId());
                userMessageLogDao.deleteMessageLogs(Arrays.asList(res.getEntityId()));
            });
        }
    }

    @Test
    @Transactional
    public void testNotifyMessageReceivedSync() throws SOAPException, IOException, ParserConfigurationException, SAXException, EbMS3Exception {
        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageHasStatus(messageId, MessageStatus.NOT_FOUND);

        assertEquals(backendConnector.getDeliverMessageEvent().getMessageId(), messageId);

        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);

        assertEquals(backendConnector.getDeliverMessageEvent().getMessageId(), messageId);
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
        MessageLogResultRO result = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters);
        assertNotNull(result);
        assertEquals(1, result.getMessageLogEntries().size());
        assertEquals(messageId, result.getMessageLogEntries().get(0).getMessageId());
    }

    @Test
    public void testNotifyMessageDeleted() throws MessagingProcessingException {
        String messageId = itTestsService.sendMessageWithStatus(MessageStatus.ACKNOWLEDGED);

        deleteMessages();

        assertEquals(backendConnector.getMessageDeletedBatchEvent().getMessageDeletedEvents().size(), 1);
        assertEquals(backendConnector.getMessageDeletedBatchEvent().getMessageDeletedEvents().get(0).getMessageId(), messageId);

        Assert.assertNull(userMessageDao.findByMessageId(messageId));
        try {
            userMessageLogDao.findByMessageId(messageId);
            Assert.fail();
        } catch (NoResultException e) {
            //OK
        }
    }

    @Test
    public void testNotifyOfSendSuccess() throws MessagingProcessingException, EbMS3Exception {
        domibusPropertyProvider.setProperty(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, "false");
        domibusPropertyProvider.setProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, "false");

        Mockito.when(mshDispatcher.dispatch(Mockito.any(SOAPMessage.class), Mockito.any(String.class), Mockito.any(Policy.class), Mockito.any(LegConfiguration.class), Mockito.any(String.class)))
                .thenReturn(Mockito.mock(SOAPMessage.class));

        ResponseResult responseResult = Mockito.mock(ResponseResult.class);
        Mockito.when(responseResult.getResponseStatus()).thenReturn(ResponseHandler.ResponseStatus.OK);
        Mockito.when(responseHandler.verifyResponse(Mockito.any(SOAPMessage.class), Mockito.any(String.class)))
                .thenReturn(responseResult);

        Mockito.when(reliabilityChecker.check(Mockito.any(SOAPMessage.class), Mockito.any(SOAPMessage.class), Mockito.any(ResponseResult.class), Mockito.any(LegConfiguration.class)))
                .thenReturn(ReliabilityChecker.CheckResult.OK);

        String messageId = itTestsService.sendMessageWithStatus(MessageStatus.SEND_ENQUEUED);

        waitUntilMessageHasStatus(messageId, MessageStatus.ACKNOWLEDGED);

        assertEquals(backendConnector.getPayloadSubmittedEvent().getMessageId(), messageId);
        assertEquals(backendConnector.getPayloadProcessedEvent().getMessageId(), messageId);

        UserMessage byMessageId = userMessageDao.findByMessageId(messageId);
        Assert.assertNotNull(byMessageId);

        deleteMessages();
    }

    @Test
    public void testNotifyOfSendFailure() throws MessagingProcessingException, EbMS3Exception, InterruptedException {
        domibusPropertyProvider.setProperty(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, "false");
        domibusPropertyProvider.setProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, "false");

        Mockito.when(mshDispatcher.dispatch(Mockito.any(SOAPMessage.class), Mockito.any(String.class), Mockito.any(Policy.class), Mockito.any(LegConfiguration.class), Mockito.any(String.class)))
                .thenReturn(Mockito.mock(SOAPMessage.class));

        ResponseResult responseResult = Mockito.mock(ResponseResult.class);
        Mockito.when(responseResult.getResponseStatus()).thenReturn(ResponseHandler.ResponseStatus.OK);
        Mockito.when(responseHandler.verifyResponse(Mockito.any(SOAPMessage.class), Mockito.any(String.class)))
                .thenReturn(responseResult);

        Mockito.when(reliabilityChecker.check(Mockito.any(SOAPMessage.class), Mockito.any(SOAPMessage.class), Mockito.any(ResponseResult.class), Mockito.any(LegConfiguration.class)))
                .thenReturn(ReliabilityChecker.CheckResult.OK);

        Mockito.when(backendConnectorService.getRequiredNotificationTypeList(Mockito.any(BackendConnector.class)))
                .thenReturn(DEFAULT_PUSH_NOTIFICATIONS);

        String messageId = itTestsService.sendMessageWithStatus(MessageStatus.WAITING_FOR_RETRY);
        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        userMessageLog.setScheduled(false);
        userMessageLog.setSendAttempts(5);
        NotificationStatusEntity entity = notificationStatusDao.findOrCreate(NotificationStatus.REQUIRED);
        userMessageLog.setNotificationStatus(entity);
        userMessageLogDao.update(userMessageLog);

        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        messageSenderErrorHandler.handleError(new Exception());

        waitUntilMessageHasStatus(messageId, MessageStatus.SEND_FAILURE);
        assertEquals(backendConnector.getMessageSendFailedEvent().getMessageId(), messageId);

        deleteMessages();
    }

}
