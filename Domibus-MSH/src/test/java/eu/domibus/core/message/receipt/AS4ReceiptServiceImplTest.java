package eu.domibus.core.message.receipt;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.pmode.validation.PropertyProfileValidator;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.util.xml.XMLUtilImpl;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class AS4ReceiptServiceImplTest {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(AS4ReceiptServiceImplTest.class);

    @Tested
    AS4ReceiptServiceImpl as4ReceiptService;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    protected NonRepudiationService nonRepudiationService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    MessagingService messagingService;

    @Injectable
    SignalMessageDao signalMessageDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageFactory messageFactory;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    JAXBContext jaxbContextEBMS;

    @Injectable
    TransformerFactory transformerFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    TimestampDateFormatter timestampDateFormatter;

    @Injectable
    CompressionService compressionService;

    @Injectable
    MessageIdGenerator messageIdGenerator;

    @Injectable
    PayloadProfileValidator payloadProfileValidator;

    @Injectable
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    CertificateService certificateService;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Injectable
    RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    protected UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    protected MessageUtil messageUtil;

    @Injectable
    protected MessageGroupDao messageGroupDao;

    @Injectable
    protected UserMessageService userMessageService;

    @Injectable
    protected UserMessageHandlerService userMessageHandlerService;

    @Injectable
    protected SoapUtil soapUtil;


    @Test
    public void testGenerateReceipt_WithReliabilityAndResponseRequired(@Injectable final LegConfiguration legConfiguration,
                                                                       @Injectable final Source messageToReceiptTransform,
                                                                       @Injectable final Transformer transformer,
                                                                       @Injectable final DOMResult domResult,
                                                                       @Injectable Messaging messaging,
                                                                       @Mocked XMLUtilImpl xmlUtil,
                                                                       @Injectable MessageFactory messageFactory,
                                                                       @Injectable Templates templates) throws Exception {
        new Expectations(as4ReceiptService) {{
            messageFactory.createMessage();
            result = soapResponseMessage;

            XMLUtilImpl.getMessageFactory();
            result = messageFactory;

            messageFactory.createMessage();
            result = soapResponseMessage;

            as4ReceiptService.getTemplates();
            result = templates;

            templates.newTransformer();
            result = transformer;

            messageIdGenerator.generateMessageId();
            result = "1234";

            timestampDateFormatter.generateTimestamp();
            result = "mydate";


            //Expecting that the saveResponse call will be invoked without any exception.
            as4ReceiptService.saveResponse(withAny(soapResponseMessage), false);
            result = any;
        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.RESPONSE, false, false, false);
        } catch (Exception e) {
            LOGGER.error("No exception was expected with valid configuration input !!!", e);
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            as4ReceiptService.saveResponse(withAny(soapResponseMessage), false);
            times = 1;
        }};
    }

    @Test
    public void testGenerateReceipt_NoResponse(@Injectable Messaging messaging) {
        new Expectations(as4ReceiptService) {{

        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.CALLBACK, false, false, false);
        } catch (Exception e) {
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            as4ReceiptService.saveResponse(withAny(soapResponseMessage), false);
            times = 0;
        }};
    }

    @Test
    public void testGenerateReceipt_TransformException(@Injectable final LegConfiguration legConfiguration,
                                                       @Injectable final Source messageToReceiptTransform,
                                                       @Injectable final Transformer transformer,
                                                       @Injectable final DOMResult domResult,
                                                       @Injectable Messaging messaging,
                                                       @Mocked XMLUtilImpl xmlUtil,
                                                       @Injectable MessageFactory messageFactory,
                                                       @Injectable Templates templates)
            throws SOAPException, TransformerException, IOException {
        new Expectations(as4ReceiptService) {{
            XMLUtilImpl.getMessageFactory();
            result = messageFactory;

            messageFactory.createMessage();
            result = soapResponseMessage;

            as4ReceiptService.getTemplates();
            result = templates;

            templates.newTransformer();
            result = transformer;

            messageIdGenerator.generateMessageId();
            result = "1234";

            timestampDateFormatter.generateTimestamp();
            result = "mydate";

            transformer.transform(withAny(messageToReceiptTransform), withAny(domResult));
            result = new TransformerException("TEST Transformer Exception");
        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.RESPONSE, false, false, false);
            Assert.fail("Expected Transformer exception to be raised !!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0201, e.getErrorCode());
        }

        new Verifications() {{
            as4ReceiptService.saveResponse(withAny(soapResponseMessage), anyBoolean);
            times = 0;
        }};
    }


    @Test
    public void testSaveResponse(@Injectable final Messaging receiptMessage) throws SOAPException, EbMS3Exception {
        new Expectations() {{

            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = receiptMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = receiptMessage;
        }};

        as4ReceiptService.saveResponse(soapResponseMessage, false);

        new Verifications() {{
            signalMessageDao.create(receiptMessage.getSignalMessage());

            signalMessageLogDao.create(withInstanceOf(SignalMessageLog.class));

            messagingDao.update(receiptMessage);
        }};
    }


    @Test
    public void testSaveResponse_DBWriteExceptionFlow(@Injectable final Messaging receiptMessage,
                                                      @Injectable final Messaging sentMessage,
                                                      @Injectable final SignalMessageLog signalMessageLog) throws Exception {
        new Expectations() {{
            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = receiptMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = sentMessage;

            signalMessageLogDao.create(withAny(signalMessageLog));
            result = new RuntimeException();
        }};

        try {
            as4ReceiptService.saveResponse(soapResponseMessage, false);
            Assert.fail("Expected failure propagation during DB commit failure!");
        } catch (Exception e) {
            Assert.assertTrue("Expected Runtime exception mocked!", e instanceof RuntimeException);
        }

        new Verifications() {{
            signalMessageDao.create(receiptMessage.getSignalMessage());
            signalMessageLogDao.create(withInstanceOf(SignalMessageLog.class));
            messagingDao.update(sentMessage);
        }};
    }

    @Test
    public void testGenerateReceipt_NoReliability(@Injectable final LegConfiguration legConfiguration,
                                                  @Injectable Messaging messaging,
                                                  @Injectable SignalMessageLog messageLog) {
        new Expectations(as4ReceiptService) {{
        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.CALLBACK, false, false, false);
        } catch (Exception e) {
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            //verify that saveResponse is not invoked
            as4ReceiptService.saveResponse(withAny(soapResponseMessage), false);
            times = 0;

            signalMessageLogDao.create(withAny(messageLog));
            times = 0;
        }};
    }

    @Test
    public void testSaveResponse_SendToSameAP(@Injectable final Messaging receiptMessage,
                                              @Injectable final Messaging sentMessage) throws Exception {
        new Expectations() {{
            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = receiptMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = sentMessage;
        }};

        as4ReceiptService.saveResponse(soapResponseMessage, true);

        new Verifications() {{
            signalMessageDao.create(receiptMessage.getSignalMessage());
            signalMessageLogDao.create(withInstanceOf(SignalMessageLog.class));
            messagingDao.update(sentMessage);
        }};
    }

    @Test
    public void testSetMessagingId(@Injectable SOAPMessage responseMessage,
                                   @Injectable UserMessage userMessage,
                                   @Injectable Iterator childElements,
                                   @Injectable SOAPElement messagingElement) throws Exception {
        String messageId = "123";
        new Expectations() {{
            responseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = childElements;

            childElements.hasNext();
            result = true;

            childElements.next();
            result = messagingElement;

            userMessage.getMessageInfo().getMessageId();
            result = messageId;
        }};

        as4ReceiptService.setMessagingId(responseMessage, userMessage);

        new Verifications() {{
            QName idQname = null;
            String value = null;
            messagingElement.addAttribute(idQname = withCapture(), value = withCapture());
            Assert.assertNotNull(idQname);
            Assert.assertEquals(idQname.getLocalPart(), "Id");
            Assert.assertEquals(idQname.getPrefix(), "wsu");
            Assert.assertNotNull(value);
            Assert.assertEquals(value, "_1" + DigestUtils.sha256Hex(messageId));
        }};
    }

}