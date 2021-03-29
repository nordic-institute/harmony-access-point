package eu.domibus.core.message.receipt;

import eu.domibus.api.model.*;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.ObjectFactory;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.nonrepudiation.NonRepudiationConstants;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.core.util.xml.XMLUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class AS4ReceiptServiceImpl implements AS4ReceiptService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AS4ReceiptServiceImpl.class);

    private static final String XSLT_GENERATE_AS4_RECEIPT_XSL = "xslt/GenerateAS4Receipt.xsl";

    protected Templates templates;
    protected byte[] as4ReceiptXslBytes;

    protected final UIReplicationSignalService uiReplicationSignalService;
    protected final UserMessageHandlerService userMessageHandlerService;
    private final TimestampDateFormatter timestampDateFormatter;
    private final SignalMessageLogDao signalMessageLogDao;
    protected final UserMessageService userMessageService;
    private final MessageIdGenerator messageIdGenerator;
    protected final UserMessageRawEnvelopeDao rawEnvelopeLogDao;
    private final SignalMessageDao signalMessageDao;
    protected final MessageGroupDao messageGroupDao;
    private final UserMessageDao userMessageDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;
    protected XMLUtil xmlUtil;
    protected Ebms3Converter ebms3Converter;
    protected MshRoleDao mshRoleDao;
    protected MessageStatusDao messageStatusDao;

    public AS4ReceiptServiceImpl(UIReplicationSignalService uiReplicationSignalService,
                                 UserMessageHandlerService userMessageHandlerService,
                                 TimestampDateFormatter timestampDateFormatter,
                                 SignalMessageLogDao signalMessageLogDao,
                                 UserMessageService userMessageService,
                                 MessageIdGenerator messageIdGenerator,
                                 UserMessageRawEnvelopeDao rawEnvelopeLogDao,
                                 SignalMessageDao signalMessageDao,
                                 MessageGroupDao messageGroupDao,
                                 UserMessageDao userMessageDao,
                                 MessageUtil messageUtil,
                                 SoapUtil soapUtil,
                                 XMLUtil xmlUtil,
                                 Ebms3Converter ebms3Converter,
                                 MshRoleDao mshRoleDao,
                                 MessageStatusDao messageStatusDao) {
        this.uiReplicationSignalService = uiReplicationSignalService;
        this.userMessageHandlerService = userMessageHandlerService;
        this.timestampDateFormatter = timestampDateFormatter;
        this.signalMessageLogDao = signalMessageLogDao;
        this.userMessageService = userMessageService;
        this.messageIdGenerator = messageIdGenerator;
        this.rawEnvelopeLogDao = rawEnvelopeLogDao;
        this.signalMessageDao = signalMessageDao;
        this.messageGroupDao = messageGroupDao;
        this.userMessageDao = userMessageDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
        this.xmlUtil = xmlUtil;
        this.ebms3Converter = ebms3Converter;
        this.mshRoleDao = mshRoleDao;
        this.messageStatusDao = messageStatusDao;
    }

    @Override
    public SOAPMessage generateReceipt(String messageId, final Boolean nonRepudiation) throws EbMS3Exception {
        final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        SOAPMessage request;
        try {
            request = soapUtil.createSOAPMessage(rawXmlByMessageId.getRawMessage());
        } catch (SOAPException | IOException | ParserConfigurationException | SAXException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", messageId, e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        return generateReceipt(request, userMessage, ReplyPattern.RESPONSE, nonRepudiation, false, false);
    }


    @Override
    @Timer(clazz = AS4ReceiptServiceImpl.class, value = "generateReceipt")
    @Counter(clazz = AS4ReceiptServiceImpl.class, value = "generateReceipt")
    public SOAPMessage generateReceipt(final SOAPMessage request,
                                       final UserMessage userMessage,
                                       final ReplyPattern replyPattern,
                                       final Boolean nonRepudiation,
                                       final Boolean duplicate,
                                       final Boolean selfSendingFlag) throws EbMS3Exception {
        SOAPMessage responseMessage = null;

        if (ReplyPattern.RESPONSE.equals(replyPattern)) {
            LOG.debug("Generating receipt for incoming message");
            try {
                responseMessage = xmlUtil.getMessageFactorySoap12().createMessage();


                String messageId;
                String timestamp;

                Source requestMessage;
                if (duplicate) {
                    final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(userMessage.getMessageId());
                    SignalMessage existingSignalMessage = signalMessageDao.findSignalMessageByUserMessageEntityId(userMessage.getEntityId());
                    messageId = existingSignalMessage.getSignalMessageId();
                    timestamp = timestampDateFormatter.generateTimestamp(existingSignalMessage.getTimestamp());
                    requestMessage = new StreamSource(new StringReader(rawXmlByMessageId.getRawMessage()));
                } else {
                    messageId = messageIdGenerator.generateMessageId();
                    timestamp = timestampDateFormatter.generateTimestamp();
                    requestMessage = request.getSOAPPart().getContent();
                }


                final Transformer transformer = getTemplates().newTransformer();
                transformer.setParameter("messageid", messageId);
                transformer.setParameter("timestamp", timestamp);
                transformer.setParameter("nonRepudiation", Boolean.toString(nonRepudiation));

                final DOMResult domResult = new DOMResult();
                transformer.transform(requestMessage, domResult);
                responseMessage.getSOAPPart().setContent(new DOMSource(domResult.getNode()));

                setMessagingId(responseMessage, userMessage);

                if (!duplicate) {
                    saveResponse(responseMessage, userMessage, selfSendingFlag);
                }

                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIPT_GENERATED, nonRepudiation);
            } catch (TransformerConfigurationException | SOAPException | IOException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Error generating receipt", e);
            } catch (final TransformerException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", userMessage.getMessageId(), e);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
        return responseMessage;
    }

    /**
     * save response in the DB before sending it back
     *
     * @param responseMessage SOAP response message
     * @param selfSendingFlag indicates that the message is sent to the same Domibus instance
     */
    protected void saveResponse(final SOAPMessage responseMessage, UserMessage userMessage, boolean selfSendingFlag) throws EbMS3Exception, SOAPException {
        LOG.debug("Saving response, self sending  [{}]", selfSendingFlag);

        Ebms3Messaging ebms3Messaging = messageUtil.getMessagingWithDom(responseMessage);
        final SignalMessage signalMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getSignalMessage());

        if (selfSendingFlag) {
                /*we add a defined suffix in order to assure DB integrity - messageId unicity
                basically we are generating another messageId for Signal Message on receiver side
                */
            signalMessage.setRefToMessageId(signalMessage.getRefToMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
            signalMessage.setSignalMessageId(signalMessage.getSignalMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
        }
        signalMessage.setUserMessage(userMessage);



        LOG.debug("Save signalMessage with messageId [{}], refToMessageId [{}]", signalMessage.getSignalMessageId(), signalMessage.getRefToMessageId());
        // Stores the signal message
        signalMessageDao.create(signalMessage);
        // Updating the reference to the signal message

        MessageStatusEntity messageStatus = messageStatusDao.findMessageStatus(MessageStatus.ACKNOWLEDGED);
        MSHRoleEntity role = mshRoleDao.findByRole(MSHRole.SENDING);

        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setSignalMessage(signalMessage)
                .setMessageStatus(messageStatus)
                .setMshRole(role);
        // Saves an entry of the signal message log
        SignalMessageLog signalMessageLog = smlBuilder.build();
        signalMessageLogDao.create(signalMessageLog);

        uiReplicationSignalService.signalMessageSubmitted(signalMessage.getSignalMessageId());
    }


    protected void setMessagingId(SOAPMessage responseMessage, UserMessage userMessage) throws SOAPException {
        final Iterator childElements = responseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
        if (childElements == null || !childElements.hasNext()) {
            LOG.warn("Could not set the Messaging Id value");
            return;
        }

        final SOAPElement messagingElement = (SOAPElement) childElements.next();
        messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, "_1" + DigestUtils.sha256Hex(userMessage.getMessageId()));
    }

    protected Templates getTemplates() throws IOException, TransformerConfigurationException {
        if (templates == null) {
            LOG.debug("Initializing the templates instance");
            InputStream generateAS4ReceiptStream = getAs4ReceiptXslInputStream();
            Source messageToReceiptTransform = new StreamSource(generateAS4ReceiptStream);
            templates = createTransformerFactoryForTemplates().newTemplates(messageToReceiptTransform);
        }
        return templates;
    }

    protected TransformerFactory createTransformerFactoryForTemplates() {
        return XMLUtilImpl.createTransformerFactory();
    }

    protected InputStream getAs4ReceiptXslInputStream() throws IOException {
        return new ByteArrayInputStream(getAs4ReceiptXslBytes());
    }

    protected byte[] getAs4ReceiptXslBytes() throws IOException {
        if (as4ReceiptXslBytes == null) {
            as4ReceiptXslBytes = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(XSLT_GENERATE_AS4_RECEIPT_XSL));
        }
        return as4ReceiptXslBytes;
    }
}
