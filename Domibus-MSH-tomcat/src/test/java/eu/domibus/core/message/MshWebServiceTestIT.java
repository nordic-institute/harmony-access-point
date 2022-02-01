package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3MessageInfo;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.*;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.SignalMessageRawEnvelopeDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MshWebServiceTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MshWebServiceTestIT.class);

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

    @Before
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();
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

        waitUntilMessageIsReceived(messageId);

        //receive the same message again
        soapResponse = mshWebserviceTest.invoke(soapMessage);
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
