package eu.domibus.core.message.plugin.handler;

import eu.domibus.AbstractIT;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MessageEvent;
import eu.domibus.common.MessageSendSuccessEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.common.PayloadSubmittedEvent;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.MessageSenderService;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.jms.JMSManagerImpl;
import eu.domibus.core.message.MessagesLogServiceImpl;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryAssertionUtil;
import eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryClientSpecification;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.notification.PluginAsyncNotificationConfiguration;
import eu.domibus.test.common.SoapSampleUtil;
import eu.domibus.test.common.SubmissionUtil;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTYINFO_ROLES_VALIDATION_ENABLED;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_KEYSTORE_NAME;
import static eu.domibus.core.crypto.MultiDomainCryptoServiceImpl.DOMIBUS_TRUSTSTORE_NAME;
import static eu.domibus.core.pmode.provider.dynamicdiscovery.DynamicDiscoveryServicePEPPOLConfigurationMockup.DOMAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

@Transactional
public class MessageSubmitterTestIT extends AbstractIT {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        public BackendConnectorService backendConnectorProvider() {
            return Mockito.mock(BackendConnectorService.class);
        }
    }

    @Autowired
    DynamicDiscoveryAssertionUtil dynamicDiscoveryAssertionUtil;

    @Autowired
    protected SubmissionUtil submissionUtil;

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Autowired
    MessageSubmitter messageSubmitter;

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    UserMessageDefaultService userMessageDefaultService;

    @Autowired
    protected PayloadFileStorageProvider payloadFileStorageProvider;

    @Autowired
    MessageSenderService messageSenderService;

    @Autowired
    MSHDispatcher mshDispatcher;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    ReliabilityChecker reliabilityChecker;

    @Autowired
    BackendConnectorHelper backendConnectorHelper;

    @Autowired
    PluginAsyncNotificationConfiguration pluginAsyncNotificationConfiguration;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    MultiDomainCryptoService multiDomainCryptoService;

    @Autowired
    DomainContextProvider domainContextProvider;

    BackendConnector backendConnector = Mockito.mock(BackendConnector.class);

    @Before
    public void before() {
        Mockito.when(backendConnectorHelper.getRequiredNotificationTypeList(backendConnector)).thenReturn(NotificationType.DEFAULT_PUSH_NOTIFICATIONS);
        Mockito.when(backendConnectorProvider.getBackendConnector(any(String.class))).thenReturn(backendConnector);

        payloadFileStorageProvider.initialize();
    }

    @Test
    public void messageSendSuccessWithStaticDiscovery() throws MessagingProcessingException, IOException, EbMS3Exception, SOAPException, ParserConfigurationException, SAXException {
        //we save the JMS manager to restore it later
        Object saveField = ReflectionTestUtils.getField(userMessageDefaultService, "jmsManager");
        ReflectionTestUtils.setField(userMessageDefaultService, "jmsManager", new JMSManagerImpl() {
            public void sendMessageToQueue(JmsMessage message, Queue destination) {
                //we simulate the dispatch of the JMS message
                String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
                Long messageEntityId = Long.valueOf(message.getStringProperty(MessageConstants.MESSAGE_ENTITY_ID));
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                messageSenderService.sendUserMessage(messageId, messageEntityId, 0);
            }
        });

        final SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage("validAS4Response.xml", "123");
        Mockito.when(mshDispatcher.dispatch(any(SOAPMessage.class), anyString(), any(Policy.class), any(LegConfiguration.class), anyString())).thenReturn(soapMessage);

        //reliability is OK
        Mockito.when(reliabilityChecker.check(any(SOAPMessage.class), any(SOAPMessage.class), any(ResponseResult.class), any(LegConfiguration.class))).thenReturn(ReliabilityChecker.CheckResult.OK);

        Submission submission = submissionUtil.createSubmission();
        uploadPmode();
        final String messageId = messageSubmitter.submit(submission, "mybackend");

        assertUserMessageAndUserMessageLogAfterSubmission(messageId, submission, "domibus-red");

        //put the real manager back
        ReflectionTestUtils.setField(userMessageDefaultService, "jmsManager", saveField);

        //reset the mocks so that they don't interfere with other tests
        Mockito.reset(mshDispatcher);
        Mockito.reset(reliabilityChecker);
    }

    private void assertUserMessageAndUserMessageLogAfterSubmission(String messageId, Submission submission, String expectedPartyTo) {
        //check that PayloadSubmittedEvent was called
        ArgumentCaptor<PayloadSubmittedEvent> payloadSubmittedEventCaptor = ArgumentCaptor.forClass(PayloadSubmittedEvent.class);
        Mockito.verify(backendConnector, Mockito.times(1)).payloadSubmittedEvent(payloadSubmittedEventCaptor.capture());
        final PayloadSubmittedEvent submittedEvent = payloadSubmittedEventCaptor.getValue();
        assertSubmittedEvent(submittedEvent, messageId, expectedPartyTo);

        //check that MessageSendSuccessEvent was called
        ArgumentCaptor<MessageSendSuccessEvent> messageSendSuccessEventCaptor = ArgumentCaptor.forClass(MessageSendSuccessEvent.class);
        Mockito.verify(backendConnector, Mockito.times(1)).messageSendSuccess(messageSendSuccessEventCaptor.capture());
        final MessageSendSuccessEvent sendSuccessEvent = messageSendSuccessEventCaptor.getValue();
        assertSubmittedEvent(sendSuccessEvent, messageId, expectedPartyTo);
        assertNotNull(sendSuccessEvent.getMessageEntityId());

        //check the UserMessageLog
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        assertNotNull(userMessageLog);
        assertEquals(MessageStatus.ACKNOWLEDGED, userMessageLog.getMessageStatus());
        assertEquals(MSHRole.SENDING, userMessageLog.getMshRole().getRole());
        assertNotNull(userMessageLog.getAcknowledged());

        //check the UserMessage
        final UserMessage userMessage = userMessageDao.findByEntityId(userMessageLog.getEntityId());
        assertNotNull(userMessage);
        assertEquals(submission.getRefToMessageId(), userMessage.getRefToMessageId());
        assertEquals(submission.getAction(), userMessage.getActionValue());
        assertEquals(submission.getService(), userMessage.getServiceValue());

        //check that we can retrieve the message by simulating the UI
        final HashMap<String, Object> filters = new HashMap<>();
        filters.put("receivedTo", new Date());
        messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 10, "received", false, filters, Collections.emptyList());
    }

    protected void assertSubmittedEvent(MessageEvent messageEvent, String expectedMessageId, String expectedPartyTo) {
        assertEquals(expectedMessageId, messageEvent.getMessageId());
        assertEquals("domibus-blue", messageEvent.getProps().get(MessageConstants.FROM_PARTY_ID));
        assertEquals(expectedPartyTo, messageEvent.getProps().get(MessageConstants.TO_PARTY_ID));
    }

    @Test
    public void messageSendSuccessWithDynamicDiscovery() throws MessagingProcessingException, IOException, EbMS3Exception, SOAPException, ParserConfigurationException, SAXException, KeyStoreException {
        //we save the JMS manager to restore it later
        Object saveField = ReflectionTestUtils.getField(userMessageDefaultService, "jmsManager");
        ReflectionTestUtils.setField(userMessageDefaultService, "jmsManager", new JMSManagerImpl() {
            public void sendMessageToQueue(JmsMessage message, Queue destination) {
                //we simulate the dispatch of the JMS message
                String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
                Long messageEntityId = Long.valueOf(message.getStringProperty(MessageConstants.MESSAGE_ENTITY_ID));
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                messageSenderService.sendUserMessage(messageId, messageEntityId, 0);
            }
        });
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        //we create the SoapResponse following the dispatch to C3
        final SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage("validAS4Response.xml", "123");
        Mockito.when(mshDispatcher.dispatch(any(SOAPMessage.class), anyString(), any(Policy.class), any(LegConfiguration.class), anyString())).thenReturn(soapMessage);

        //reliability is OK
        Mockito.when(reliabilityChecker.check(any(SOAPMessage.class), any(SOAPMessage.class), any(ResponseResult.class), any(LegConfiguration.class))).thenReturn(ReliabilityChecker.CheckResult.OK);

        //upload the Pmode for dynamic discovery
        uploadPmode(null, "dataset/pmode/PModeDynamicDiscovery.xml", null);

        //activate dynamic discovery
        domibusPropertyProvider.setProperty(DOMAIN, DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "true");
        domibusPropertyProvider.setProperty(DOMAIN, DOMIBUS_PARTYINFO_ROLES_VALIDATION_ENABLED, "false");
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DynamicDiscoveryClientSpecification.PEPPOL.getName());
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, "peppol-transport-as4-v2_0");

        //create keystore and truststore
        createStore(DOMIBUS_TRUSTSTORE_NAME, "keystores/gateway_truststore.jks");
        createStore(DOMIBUS_KEYSTORE_NAME, "keystores/gateway_keystore.jks");

        //prepare the submission
        Submission submission = submissionUtil.createSubmission();
        final Submission.TypedProperty finalRecipientProperty = submission.getMessageProperties().stream().filter(typedProperty -> typedProperty.getKey().equals("finalRecipient")).findFirst().orElseThrow(() -> new RuntimeException("Could not find final recipient"));

        final String expectedDiscoveredPartyName = "party1";

        //we set the correct party type according to the Pmode
        submission.getFromParties().clear();
        submission.getFromParties().add(new Submission.Party("domibus-blue", "urn:fdc:peppol.eu:2017:identifiers:ap"));
        final String finalRecipient = "0208:1111";
        finalRecipientProperty.setValue(finalRecipient);
        final String messageId = messageSubmitter.submit(submission, "mybackend");

        //we check that the discovered certificate was added to the truststore by the dynamic discovery mechanism
        final X509Certificate certificateFromTruststore = multiDomainCryptoService.getCertificateFromTruststore(currentDomain, expectedDiscoveredPartyName);
        assertNotNull(certificateFromTruststore);

        //check that the party was added in the Pmode by the dynamic discovery mechanism
        dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheListOfPartiesFromPmode(2, expectedDiscoveredPartyName);
        dynamicDiscoveryAssertionUtil.verifyIfPartyIsPresentInTheListOfPartiesFromPmode(2, expectedDiscoveredPartyName);

        //check an entry was added in the lookup table by the dynamic discovery mechanism
        dynamicDiscoveryAssertionUtil.verifyThatDynamicDiscoveryLookupWasAddedInTheDatabase(1, finalRecipient, expectedDiscoveredPartyName);

        assertUserMessageAndUserMessageLogAfterSubmission(messageId, submission, expectedDiscoveredPartyName);

        //put the real manager back
        ReflectionTestUtils.setField(userMessageDefaultService, "jmsManager", saveField);

        domibusPropertyProvider.setProperty(DOMAIN, DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY, "false");
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION, DynamicDiscoveryClientSpecification.OASIS.getName());
        domibusPropertyProvider.setProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4, "bdxr-transport-ebms3-as4-v1p0");

        multiDomainCryptoService.removeCertificate(currentDomain, expectedDiscoveredPartyName);
        //reset the mocks so that they don't interfere with other tests
        Mockito.reset(mshDispatcher);
        Mockito.reset(reliabilityChecker);
    }

    @Test
    public void submitWithNoFromPartyId() throws MessagingProcessingException, IOException {
        Submission submission = submissionUtil.createSubmission();
        submission.getFromParties().clear();

        uploadPmode();
        try {
            messageSubmitter.submit(submission, "mybackend");
            Assert.fail("Messaging exception should have been thrown");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MessagingProcessingException);
            Assert.assertTrue(e.getMessage().contains("Mandatory field From PartyId is not provided"));
        }
    }

    @Test
    public void submitWithNoToPartyId() throws MessagingProcessingException, IOException {
        Submission submission = submissionUtil.createSubmission();
        submission.getToParties().clear();

        uploadPmode();
        try {
            messageSubmitter.submit(submission, "mybackend");
            Assert.fail("Messaging exception should have been thrown");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof MessagingProcessingException);
            Assert.assertTrue(e.getMessage().contains("ValueInconsistent detail: Mandatory field To PartyId is not provided"));
        }
    }


}
