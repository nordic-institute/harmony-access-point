package eu.domibus.core.message.receipt;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.nonrepudiation.NonRepudiationConstants;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeDto;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.message.signal.SignalMessageLogBuilder;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.core.util.xml.XMLUtilImpl;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
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
    protected final NonRepudiationService nonRepudiationService;
    private final SignalMessageLogDao signalMessageLogDao;
    protected final UserMessageService userMessageService;
    private final MessageIdGenerator messageIdGenerator;
    protected final RawEnvelopeLogDao rawEnvelopeLogDao;
    private final SignalMessageDao signalMessageDao;
    protected final MessageGroupDao messageGroupDao;
    private final MessagingDao messagingDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;

    public AS4ReceiptServiceImpl(UIReplicationSignalService uiReplicationSignalService,
                                 UserMessageHandlerService userMessageHandlerService,
                                 TimestampDateFormatter timestampDateFormatter,
                                 NonRepudiationService nonRepudiationService,
                                 SignalMessageLogDao signalMessageLogDao,
                                 UserMessageService userMessageService,
                                 MessageIdGenerator messageIdGenerator,
                                 RawEnvelopeLogDao rawEnvelopeLogDao,
                                 SignalMessageDao signalMessageDao,
                                 MessageGroupDao messageGroupDao,
                                 MessagingDao messagingDao,
                                 MessageUtil messageUtil,
                                 SoapUtil soapUtil) {
        this.uiReplicationSignalService = uiReplicationSignalService;
        this.userMessageHandlerService = userMessageHandlerService;
        this.timestampDateFormatter = timestampDateFormatter;
        this.nonRepudiationService = nonRepudiationService;
        this.signalMessageLogDao = signalMessageLogDao;
        this.userMessageService = userMessageService;
        this.messageIdGenerator = messageIdGenerator;
        this.rawEnvelopeLogDao = rawEnvelopeLogDao;
        this.signalMessageDao = signalMessageDao;
        this.messageGroupDao = messageGroupDao;
        this.messagingDao = messagingDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
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

        Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        return generateReceipt(request, messaging, ReplyPattern.RESPONSE, nonRepudiation, false, false);
    }


    @Override
    public SOAPMessage generateReceipt(final SOAPMessage request,
                                       final Messaging messaging,
                                       final ReplyPattern replyPattern,
                                       final Boolean nonRepudiation,
                                       final Boolean duplicate,
                                       final Boolean selfSendingFlag) throws EbMS3Exception {
        SOAPMessage responseMessage = null;
        UserMessage userMessage = messaging.getUserMessage();

        if (ReplyPattern.RESPONSE.equals(replyPattern)) {
            LOG.debug("Generating receipt for incoming message");
            try {
                responseMessage = XMLUtilImpl.getMessageFactory().createMessage();


                String messageId;
                String timestamp;

                Source requestMessage;
                if (duplicate) {
                    final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(userMessage.getMessageInfo().getMessageId());
                    Messaging existingMessage = messagingDao.findMessageByMessageId(userMessage.getMessageInfo().getMessageId());
                    messageId = existingMessage.getSignalMessage().getMessageInfo().getMessageId();
                    timestamp = timestampDateFormatter.generateTimestamp(existingMessage.getSignalMessage().getMessageInfo().getTimestamp());
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
                    saveResponse(responseMessage, selfSendingFlag);
                }

                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIPT_GENERATED, nonRepudiation);
            } catch (TransformerConfigurationException | SOAPException | IOException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Error generating receipt", e);
            } catch (final TransformerException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0201, "Could not generate Receipt. Check security header and non-repudiation settings", userMessage.getMessageInfo().getMessageId(), e);
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
    protected void saveResponse(final SOAPMessage responseMessage, boolean selfSendingFlag) throws EbMS3Exception, SOAPException {
        LOG.debug("Saving response, self sending  [{}]", selfSendingFlag);

        Messaging messaging = messageUtil.getMessagingWithDom(responseMessage);
        final SignalMessage signalMessage = messaging.getSignalMessage();

        if (selfSendingFlag) {
                /*we add a defined suffix in order to assure DB integrity - messageId unicity
                basically we are generating another messageId for Signal Message on receiver side
                */
            signalMessage.getMessageInfo().setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
            signalMessage.getMessageInfo().setMessageId(signalMessage.getMessageInfo().getMessageId() + UserMessageHandlerService.SELF_SENDING_SUFFIX);
        }
        LOG.debug("Save signalMessage with messageId [{}], refToMessageId [{}]", signalMessage.getMessageInfo().getMessageId(), signalMessage.getMessageInfo().getRefToMessageId());
        // Stores the signal message
        signalMessageDao.create(signalMessage);
        // Updating the reference to the signal message
        Messaging sentMessage = messagingDao.findMessageByMessageId(messaging.getSignalMessage().getMessageInfo().getRefToMessageId());
        MessageSubtype messageSubtype = null;
        if (sentMessage != null) {
            LOG.debug("Updating the reference to the signal message [{}]", sentMessage.getUserMessage().getMessageInfo().getMessageId());
            if (userMessageHandlerService.checkTestMessage(sentMessage.getUserMessage())) {
                messageSubtype = MessageSubtype.TEST;
            }
            sentMessage.setSignalMessage(signalMessage);
            messagingDao.update(sentMessage);
        }
        // Builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(messaging.getSignalMessage().getMessageInfo().getMessageId())
                .setMessageStatus(MessageStatus.ACKNOWLEDGED)
                .setMshRole(MSHRole.SENDING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED);
        // Saves an entry of the signal message log
        SignalMessageLog signalMessageLog = smlBuilder.build();
        signalMessageLog.setMessageSubtype(messageSubtype);
        signalMessageLogDao.create(signalMessageLog);

        uiReplicationSignalService.signalMessageSubmitted(signalMessageLog.getMessageId());
    }


    protected void setMessagingId(SOAPMessage responseMessage, UserMessage userMessage) throws SOAPException {
        final Iterator childElements = responseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
        if (childElements == null || !childElements.hasNext()) {
            LOG.warn("Could not set the Messaging Id value");
            return;
        }

        final SOAPElement messagingElement = (SOAPElement) childElements.next();
        messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, "_1" + DigestUtils.sha256Hex(userMessage.getMessageInfo().getMessageId()));
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
