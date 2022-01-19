package eu.domibus.core.message;

import eu.domibus.ITTestsService;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.*;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.reliability.ReliabilityChecker;
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
import eu.domibus.test.common.BackendConnectorMock;
import eu.domibus.test.common.SoapSampleUtil;
import eu.domibus.test.common.SubmissionUtil;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.util.ReflectionTestUtils;
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
import java.util.UUID;
import eu.domibus.plugin.ProcessingType;

import static eu.domibus.common.NotificationType.DEFAULT_PUSH_NOTIFICATIONS;
import static eu.domibus.jms.spi.InternalJMSConstants.UNKNOWN_RECEIVER_QUEUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;

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

    BackendConnectorMock backendConnector;
    String messageId, filename;

    @Before
    public void before() throws IOException, XmlProcessingException {
        MessageExchangeService mock = Mockito.mock(MessageExchangeService.class);
        ReflectionTestUtils.setField(this, "messageExchangeService", mock);

        messageId = UUID.randomUUID() + "@domibus.eu";
        filename = "SOAPMessage2.xml";

        uploadPmode();

        backendConnector = new BackendConnectorMock("domibusBackend");
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class)))
                .thenReturn(backendConnector);
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
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.SEND_ENQUEUED);
        Mockito.when(messageExchangeService.getMessageStatus(Mockito.any(MessageExchangeConfiguration.class), Mockito.any(ProcessingType.class)))
                .thenReturn(messageStatusEntity);

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
        assertEquals(result.getMessageLogEntries().size(), 1);
        assertEquals(result.getMessageLogEntries().get(0).getMessageId(), messageId);
    }

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Test
    public void testDeleteFailedMessage() throws MessagingProcessingException, EbMS3Exception {
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.SEND_ENQUEUED);

        Mockito.when(messageExchangeService.forcePullOnMpc(Mockito.any(UserMessage.class))).thenReturn(false);
        Mockito.when(messageExchangeService.getMessageStatus(Mockito.any(MessageExchangeConfiguration.class), Mockito.any(ProcessingType.class)))
                .thenReturn(messageStatusEntity);
        doNothing().when(messageExchangeService).verifySenderCertificate(Mockito.any(LegConfiguration.class), Mockito.any(String.class));
        doNothing().when(messageExchangeService).verifyReceiverCertificate(Mockito.any(LegConfiguration.class), Mockito.any(String.class));

        Mockito.when(mshDispatcher.dispatch(Mockito.any(SOAPMessage.class), Mockito.any(String.class), Mockito.any(Policy.class), Mockito.any(LegConfiguration.class), Mockito.any(String.class)))
                .thenReturn(Mockito.mock(SOAPMessage.class));

        ResponseResult responseResult = Mockito.mock(ResponseResult.class);
        Mockito.when(responseResult.getResponseStatus()).thenReturn(ResponseHandler.ResponseStatus.OK);
        Mockito.when(responseHandler.verifyResponse(Mockito.any(SOAPMessage.class), Mockito.any(String.class)))
                .thenReturn(responseResult);

        Mockito.when(reliabilityChecker.check(Mockito.any(SOAPMessage.class), Mockito.any(SOAPMessage.class), Mockito.any(ResponseResult.class), Mockito.any(LegConfiguration.class)))
                .thenReturn(ReliabilityChecker.CheckResult.OK);

        String messageId = itTestsService.sendMessageToDelete(MessageStatus.SEND_ENQUEUED);

        waitUntilMessageHasStatus(messageId, MessageStatus.WAITING_FOR_RETRY);

        UserMessage byMessageId = userMessageDao.findByMessageId(messageId);
        Assert.assertNotNull(byMessageId);

        deleteMessages();

//        UserMessage byMessageId1 = userMessageDao.findByMessageId(messageId);
//        Assert.assertNull(byMessageId1);
//        try {
//            userMessageLogDao.findByMessageId(messageId);
//            Assert.fail();
//        } catch (NoResultException e) {
//            //OK
//        }
    }
}
