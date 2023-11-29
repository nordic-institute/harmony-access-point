package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3MessageInfo;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.*;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.NotificationType;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.SignalMessageRawEnvelopeDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.test.common.BackendConnectorMock;
import eu.domibus.test.common.SoapSampleUtil;
import mockit.Injectable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

public class MshWebServiceTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MshWebServiceTestIT.class);

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorService backendConnectorProvider() {
            return Mockito.mock(BackendConnectorService.class);
        }
    }

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    MessagingService messagingService;

    @Autowired
    MSHWebservice mshWebserviceTest;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    protected UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Autowired
    protected SignalMessageDao signalMessageDao;

    @Autowired
    protected SignalMessageLogDao signalMessageLogDao;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected ReceiptDao receiptDao;

    @Autowired
    NonRepudiationService nonRepudiationService;

    @Autowired
    protected SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected RoutingService routingService;

    @Autowired
    protected PayloadFileStorageProvider payloadFileStorageProvider;

    @Autowired
    protected BackendConnectorHelper backendConnectorHelper;

    @Autowired
    protected PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Injectable
    private Queue queue;

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
        payloadFileStorageProvider.initialize();
    }

    @Test
    public void testDuplicateDetection() throws Exception {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);
        final Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(ebms3Messaging);
        final Ebms3SignalMessage firstSignalMessage = ebms3Messaging.getSignalMessage();

        //receive the same message again
        soapResponse = mshWebserviceTest.invoke(soapSampleUtil.createSOAPMessage(filename, messageId));
        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapResponse);
        assertNotNull(secondEbms3Messaging);
        final Ebms3SignalMessage secondSignalMessage = secondEbms3Messaging.getSignalMessage();
        final Ebms3MessageInfo secondMessageInfo = secondSignalMessage.getMessageInfo();

        //check that the Signal refers to the original sent message(duplicate detection works)
        assertEquals(messageId, secondMessageInfo.getRefToMessageId());
        assertEquals(firstSignalMessage.getMessageInfo().getMessageId(), secondMessageInfo.getMessageId());
        final String firstReceipt = firstSignalMessage.getReceipt().getAny().iterator().next();
        final String secondReceipt = secondSignalMessage.getReceipt().getAny().iterator().next();
        assertEquals(firstReceipt, secondReceipt);

        messageRetentionService.deleteAllMessages();
    }

    @Test
    @Ignore // TODO: FGA 2023-06-22 make other tests fail with UncategorizedJmsException 'Expected Jms Error'
    public void testAsyncNotifInError() throws Exception {
        Object saveField = ReflectionTestUtils.getField(backendNotificationService, "jmsManager");
        ReflectionTestUtils.setField(backendNotificationService, "jmsManager", new JMSManagerImpl() {
            public void sendMessageToQueue(JmsMessage message, Queue destination) {
                throw new UncategorizedJmsException("Expected Jms Error");
            }
        });

        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);
        Mockito.when(backendConnectorHelper.getRequiredNotificationTypeList(backendConnector)).thenReturn(Arrays.asList(NotificationType.MESSAGE_RECEIVED));
        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);
        Mockito.when(backendFilter.getBackendName()).thenReturn("jmsPlugin");
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendConnector()).thenReturn(new BackendConnectorMock("jmsPlugin"));
        Mockito.when(pluginAsyncNotificationConfiguration.getQueueName()).thenReturn("notificationQueue");
        Mockito.when(pluginAsyncNotificationConfiguration.getBackendNotificationQueue()).thenReturn(queue);

        String filename = "SOAPMessage2.xml";
        String messageId = UUID.randomUUID() + "@domibus.eu";
        try {

            mshWebserviceTest.invoke(soapSampleUtil.createSOAPMessage(filename, messageId));
            fail();
        } catch (RuntimeException e) {
            //do nothing
        } catch (Exception e) {
            //do nothing
        }

        UserMessageLog byMessageId = userMessageLogService.findByMessageId(messageId);

        assertNull(byMessageId);
        ReflectionTestUtils.setField(backendNotificationService, "jmsManager", saveField);
    }

    @Transactional
    @Test
    public void testGetStatusReceived() throws Exception {
        BackendConnector backendConnector = Mockito.mock(BackendConnector.class);
        Mockito.when(backendConnectorProvider.getBackendConnector(Mockito.any(String.class))).thenReturn(backendConnector);

        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        final SOAPMessage soapResponse = mshWebserviceTest.invoke(soapMessage);

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

        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        assertNotNull(userMessage);
        assertEquals(messageId, userMessage.getMessageId());

        final UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(userMessage.getEntityId());
        assertNotNull(userMessageLog);
        assertEquals(MessageStatus.RECEIVED, userMessageLog.getMessageStatus());


        final SignalMessage dbSignalMessage = signalMessageDao.findByUserMessageEntityId(userMessage.getEntityId());
        assertNotNull(dbSignalMessage);
        assertEquals(messageId, dbSignalMessage.getRefToMessageId());

        final SignalMessageLog signalMessageLog = signalMessageLogDao.read(dbSignalMessage.getEntityId());
        assertNotNull(signalMessageLog);

        final ReceiptEntity receiptEntity = receiptDao.read(dbSignalMessage.getEntityId());
        assertNotNull(receiptEntity);
        assertNotNull(receiptEntity.getRawXML());

        final UserMessageRaw userMessageRaw = rawEnvelopeLogDao.read(userMessage.getEntityId());
        assertNotNull(userMessageRaw);
        assertNotNull(userMessageRaw.getRawXML());

        String receivedUserMessageRawXml = new String(userMessageRaw.getRawXML(), StandardCharsets.UTF_8);

        final String expectedReceivedRawXml = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dataset/as4/mshwebserviceit-soapenvelope.xml"), StandardCharsets.UTF_8);
        assertEquals(expectedReceivedRawXml, receivedUserMessageRawXml);

        nonRepudiationService.saveResponse(soapResponse, userMessage.getEntityId());
        final SignalMessageRaw signalMessageRaw = signalMessageRawEnvelopeDao.read(userMessage.getEntityId());
        assertNotNull(signalMessageRaw);
        final String signalMessageRawString = new String(signalMessageRaw.getRawXML());
        LOG.info("signalMessageRawString [{}]", signalMessageRawString);

        final String expectedResponseRawXml = getExpectedResponseXml(signalMessageRawString);
        assertEquals(expectedResponseRawXml, signalMessageRawString);

        String rawXMLMessage = soapUtil.getRawXMLMessage(soapResponse);
        assertTrue(Arrays.equals(signalMessageRaw.getRawXML(), rawXMLMessage.getBytes(StandardCharsets.UTF_8)));
    }

    protected String getExpectedResponseXml(final String signalMessageRawString) throws IOException {
        final String expectedResponseRawXml = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("dataset/as4/mshwebserviceit-soapenvelope-response.xml"), StandardCharsets.UTF_8);

        final String startString = "<eb3:Timestamp>";
        final String endString = "</eb3:MessageId>";
        final int startIndex = StringUtils.indexOf(signalMessageRawString, startString);
        final int endIndex = StringUtils.indexOf(signalMessageRawString, endString) + endString.length();

        final String toReplace = StringUtils.substring(signalMessageRawString, startIndex, endIndex);
        return StringUtils.replace(expectedResponseRawXml, "PLACEHOLDER_TIMESTAMP_MESSAGEID", toReplace);
    }
}
