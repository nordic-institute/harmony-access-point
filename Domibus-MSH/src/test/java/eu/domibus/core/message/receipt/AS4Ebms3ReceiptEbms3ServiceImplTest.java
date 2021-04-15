package eu.domibus.core.message.receipt;

import mockit.integration.junit4.JMockit;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class AS4Ebms3ReceiptEbms3ServiceImplTest {

   /* @Tested
    AS4ReceiptServiceImpl as4ReceiptService;

    @Injectable
    BackendNotificationService backendNotificationService;
    @Injectable
    NonRepudiationService nonRepudiationService;
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
    UserMessageRawEnvelopeDao rawEnvelopeLogDao;
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

    @Injectable
    private LegConfiguration legConfiguration;

    @Injectable
    private Messaging messaging;

    @Injectable
    private SignalMessage signalMessage;

    @Injectable
    private MessageInfo messageInfo;

    @Injectable
    private UserMessage userMessage;

    @Injectable
    private SignalMessageLog messageLog;

    @Injectable
    private XMLUtil xmlUtil;
    @Mocked
    private SignalMessageLogBuilder signalMessageLogBuilder;

    @Injectable
    Ebms3Converter ebms3Converter;

    @Test
    public void testGenerateReceipt_WithReliabilityAndResponseRequired(@Injectable final Source messageToReceiptTransform,
                                                                       @Injectable final Transformer transformer,
                                                                       @Injectable final DOMResult domResult,
                                                                       @Injectable MessageFactory messageFactory,
                                                                       @Injectable SOAPPart soapPart,
                                                                       @Injectable Templates templates) throws Exception {
        new Expectations(as4ReceiptService) {{
            messaging.getUserMessage();
            result = userMessage;

            messageFactory.createMessage();
            result = soapResponseMessage;

            soapRequestMessage.getSOAPPart();
            result = soapPart;

            xmlUtil.getMessageFactorySoap12();
            result = messageFactory;

            messageIdGenerator.generateMessageId();
            result = "1234";

            timestampDateFormatter.generateTimestamp();
            result = "mydate";

            soapPart.getContent();
            result = messageToReceiptTransform;

            templates.newTransformer();
            result = transformer;

            soapResponseMessage.getSOAPPart();
            result = soapPart;
        }};

        as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.RESPONSE, false, false, false);

        new FullVerifications(as4ReceiptService) {{
            transformer.setParameter(anyString, any);
            times = 3;

            transformer.transform(withAny(messageToReceiptTransform), withAny(domResult));
            times = 1;

            as4ReceiptService.saveResponse(withAny(soapResponseMessage), false);
            times = 1;

            soapPart.setContent((Source) any);
            times = 1;

            as4ReceiptService.setMessagingId(soapResponseMessage, userMessage);
            times = 1;
        }};
    }

    @Test
    public void testGenerateReceipt_NoResponse() throws EbMS3Exception {

        new Expectations(as4ReceiptService) {{
            messaging.getUserMessage();
            result = userMessage;
        }};

        as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.CALLBACK, false, false, false);

        new FullVerifications() {
        };
    }

    @Test
    public void testGenerateReceipt_TransformException(@Injectable Source messageToReceiptTransform,
                                                       @Injectable Transformer transformer,
                                                       @Injectable DOMResult domResult,
                                                       @Injectable SOAPPart soapPart,
                                                       @Injectable MessageFactory messageFactory,
                                                       @Injectable Templates templates) throws Exception {
        new Expectations(as4ReceiptService) {{
            messaging.getUserMessage();
            result = userMessage;

            xmlUtil.getMessageFactorySoap12();
            result = messageFactory;

            messageFactory.createMessage();
            result = soapResponseMessage;

            soapRequestMessage.getSOAPPart();
            result = soapPart;

            soapPart.getContent();
            result = messageToReceiptTransform;

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

            userMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = "MESSAGE_ID";

        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.RESPONSE, false, false, false);
            fail("Expected Transformer exception to be raised !!!");
        } catch (EbMS3Exception e) {
            assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0201, e.getErrorCode());
        }

        new FullVerifications() {{
            transformer.setParameter(anyString, any);
            times = 3;
        }};
    }


    @Test
    public void testSaveResponse(@Injectable Ebms3Messaging ebms3Messaging) throws SOAPException, EbMS3Exception {
        new Expectations() {{

            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(ebms3Messaging.getSignalMessage());
            result = signalMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = messaging;

            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = "MESSAGE_ID";

            messageInfo.getRefToMessageId();
            result = "REF_MESSAGE_ID";

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageInfo();
            result = messageInfo;

            userMessageHandlerService.checkTestMessage(userMessage);
            result = false;

            SignalMessageLogBuilder.create();
            result = signalMessageLogBuilder;

            signalMessageLogBuilder.build();
            result = messageLog;

            messageLog.getMessageId();
            result = "MESSAGE_ID";
        }};

        as4ReceiptService.saveResponse(soapResponseMessage, false);

        new Verifications() {{
            signalMessageDao.create(signalMessage);
            signalMessageLogDao.create(withInstanceOf(SignalMessageLog.class));
            messagingDao.update(messaging);
            messaging.setSignalMessage(withInstanceOf(SignalMessage.class));
            signalMessageLogBuilder.setMessageId(anyString);
            signalMessageLogBuilder.setMessageStatus(MessageStatus.ACKNOWLEDGED);
            signalMessageLogBuilder.setMshRole(MSHRole.SENDING);
            signalMessageLogBuilder.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            messageLog.setMessageSubtype(null);
            signalMessageLogDao.create(messageLog);
            uiReplicationSignalService.signalMessageSubmitted(anyString);
        }};
    }

    @Test
    public void testSaveResponse_Test(@Injectable Ebms3Messaging ebms3Messaging) throws SOAPException, EbMS3Exception {
        new Expectations() {{

            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(ebms3Messaging.getSignalMessage());
            result = signalMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = messaging;

            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = "MESSAGE_ID";

            messageInfo.getRefToMessageId();
            result = "REF_MESSAGE_ID";

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageInfo();
            result = messageInfo;

            userMessageHandlerService.checkTestMessage(userMessage);
            result = true;

            SignalMessageLogBuilder.create();
            result = signalMessageLogBuilder;

            signalMessageLogBuilder.build();
            result = messageLog;

            messageLog.getMessageId();
            result = "MESSAGE_ID";
        }};

        as4ReceiptService.saveResponse(soapResponseMessage, false);

        new Verifications() {{
            signalMessageDao.create(signalMessage);
            messagingDao.update(messaging);
            messaging.setSignalMessage(signalMessage);
            signalMessageLogBuilder.setMessageId(anyString);
            signalMessageLogBuilder.setMessageStatus(MessageStatus.ACKNOWLEDGED);
            signalMessageLogBuilder.setMshRole(MSHRole.SENDING);
            signalMessageLogBuilder.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            messageLog.setMessageSubtype(MessageSubtype.TEST);
            signalMessageLogDao.create(messageLog);
            uiReplicationSignalService.signalMessageSubmitted(anyString);
        }};
    }

    @Test
    public void testSaveResponse_DBWriteExceptionFlow(@Injectable Ebms3Messaging ebms3Messaging) throws SOAPException, EbMS3Exception {
        new Expectations() {{

            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(ebms3Messaging.getSignalMessage());
            result = signalMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = messaging;

            messageInfo.getMessageId();
            result = "MESSAGE_ID";

            messageInfo.getRefToMessageId();
            result = "REF_MESSAGE_ID";

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageInfo();
            result = messageInfo;

            userMessageHandlerService.checkTestMessage(userMessage);
            result = true;

            SignalMessageLogBuilder.create();
            result = signalMessageLogBuilder;

            signalMessageLogBuilder.build();
            result = messageLog;

            signalMessageLogDao.create(messageLog);
            result = new SOAPException("TEST");
        }};

        try {
            as4ReceiptService.saveResponse(soapResponseMessage, false);
            fail();
        } catch (SOAPException e) {
            assertEquals(e.getMessage(), "TEST");
        }

        new Verifications() {{
            signalMessageDao.create(signalMessage);
            messagingDao.update(messaging);
            messaging.setSignalMessage(withInstanceOf(SignalMessage.class));
            signalMessageLogBuilder.setMessageId(anyString);
            signalMessageLogBuilder.setMessageStatus(MessageStatus.ACKNOWLEDGED);
            signalMessageLogBuilder.setMshRole(MSHRole.SENDING);
            signalMessageLogBuilder.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            messageLog.setMessageSubtype(MessageSubtype.TEST);
        }};
    }

    @Test
    public void testSaveResponse_selfSending(@Injectable Ebms3Messaging ebms3Messaging,
                                             @Injectable Ebms3SignalMessage ebms3SignalMessage,
                                             @Injectable Ebms3MessageInfo ebms3MessageInfo,
                                             @Injectable Ebms3UserMessage ebms3UserMessage
    ) throws SOAPException, EbMS3Exception {
        new Expectations() {{
            messageUtil.getMessagingWithDom(withAny(soapRequestMessage));
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(withAny(ebms3SignalMessage));
            result = signalMessage;

            messagingDao.findMessageByMessageId(anyString);
            result = messaging;

            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = "MESSAGE_ID";

            messageInfo.getRefToMessageId();
            result = "REF_MESSAGE_ID";

            messaging.getUserMessage();
            result = userMessage;

            userMessage.getMessageInfo();
            result = messageInfo;

            userMessageHandlerService.checkTestMessage(userMessage);
            result = false;

            SignalMessageLogBuilder.create();
            result = signalMessageLogBuilder;

            signalMessageLogBuilder.build();
            result = messageLog;

            messageLog.getMessageId();
            result = "MESSAGE_ID";


        }};

        as4ReceiptService.saveResponse(soapResponseMessage, true);

        new Verifications() {{
            messageInfo.setRefToMessageId(anyString);
            messageInfo.setMessageId(anyString);
            signalMessageDao.create(signalMessage);
            messagingDao.update(messaging);
            messaging.setSignalMessage(withInstanceOf(SignalMessage.class));
            signalMessageLogBuilder.setMessageId(anyString);
            signalMessageLogBuilder.setMessageStatus(MessageStatus.ACKNOWLEDGED);
            signalMessageLogBuilder.setMshRole(MSHRole.SENDING);
            signalMessageLogBuilder.setNotificationStatus(NotificationStatus.NOT_REQUIRED);
            messageLog.setMessageSubtype(null);
            signalMessageLogDao.create(messageLog);
            uiReplicationSignalService.signalMessageSubmitted(anyString);
        }};
    }

    @Test
    public void testGenerateReceipt_NoReliability() throws EbMS3Exception {
        new Expectations(as4ReceiptService) {{
            messaging.getUserMessage();
            result = userMessage;
        }};

        as4ReceiptService.generateReceipt(soapRequestMessage, messaging, ReplyPattern.CALLBACK, false, false, false);

        new FullVerifications() {
        };
    }

    @Test
    public void testSetMessagingId(@Injectable SOAPMessage responseMessage,
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

        new FullVerifications() {{
            QName idQname;
            String value;
            messagingElement.addAttribute(idQname = withCapture(), value = withCapture());
            assertNotNull(idQname);
            assertEquals(idQname.getLocalPart(), "Id");
            assertEquals(idQname.getPrefix(), "wsu");
            assertNotNull(value);
            assertEquals(value, "_1" + DigestUtils.sha256Hex(messageId));
        }};
    }*/

}