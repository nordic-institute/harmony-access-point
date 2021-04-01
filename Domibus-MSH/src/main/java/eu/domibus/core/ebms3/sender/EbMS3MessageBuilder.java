package eu.domibus.core.ebms3.sender;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageHeaderType;
import eu.domibus.api.ebms3.model.mf.Ebms3TypeType;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.model.splitandjoin.MessageHeaderEntity;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.exception.SendMessageException;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.UserMessageFactory;
import eu.domibus.core.message.nonrepudiation.NonRepudiationConstants;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
@Service
public class EbMS3MessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EbMS3MessageBuilder.class);

    public static final String ID_PREFIX_MESSAGING = "_1";
    public static final String ID_PREFIX_SOAP_BODY = "_2";
    public static final String ID_PREFIX_MESSAGE_FRAGMENT = "_3";

    private final ObjectFactory ebMS3Of = new ObjectFactory();


    @Autowired
    @Qualifier(value = "jaxbContextEBMS")
    protected JAXBContext jaxbContext;

    @Autowired
    @Qualifier(value = "jaxbContextMessageFragment")
    protected JAXBContext jaxbContextMessageFragment;

    @Autowired
    protected MessageIdGenerator messageIdGenerator;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected XMLUtil xmlUtil;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    public SOAPMessage buildSOAPMessage(final SignalMessage signalMessage, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPMessage(signalMessage);
    }

    public SOAPMessage buildSOAPMessage(final UserMessage userMessage, final List<PartInfo> partInfoList, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPUserMessage(userMessage, null, partInfoList, null);
    }

    public SOAPMessage buildSOAPMessageForFragment(final UserMessage userMessage, MessageFragmentEntity messageFragmentEntity, final List<PartInfo> partInfoList, MessageGroupEntity messageGroupEntity, final LegConfiguration leg) throws EbMS3Exception {
        return buildSOAPUserMessage(userMessage, messageFragmentEntity, partInfoList, messageGroupEntity);
    }

    public SOAPMessage getSoapMessage(EbMS3Exception ebMS3Exception) {
        final SignalMessage signalMessage = new SignalMessage();
        //TODO check if we still need to add the errors on the Signal Message
//        signalMessage.getError().add(ebMS3Exception.getFaultInfoError());
        try {
            return buildSOAPMessage(signalMessage, null);
        } catch (EbMS3Exception e) {
            try {
                return buildSOAPFaultMessage(e.getFaultInfoError());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        }
    }

    //TODO: If Leg is used in future releases we have to update this method
    public SOAPMessage buildSOAPFaultMessage(final Ebms3Error ebMS3error) throws EbMS3Exception {
        final SignalMessage signalMessage = new SignalMessage();
        //TODO check if we still need to add the errors on the Signal Message
//        signalMessage.getError().add(ebMS3error);

        final SOAPMessage soapMessage = this.buildSOAPMessage(signalMessage, null);

        try {
            // An ebMS signal does not require any SOAP Body: if the SOAP Body is not empty, it MUST be ignored by the MSH, as far as interpretation of the signal is concerned.
            //TODO: locale is static
            soapMessage.getSOAPBody().addFault(SOAPConstants.SOAP_RECEIVER_FAULT, "An error occurred while processing your request. Please check the message header for more details.", Locale.ENGLISH);
        } catch (final SOAPException e) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "An error occurred while processing your request. Please check the message header for more details.", signalMessage.getSignalMessageId(), e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        return soapMessage;
    }

    protected SOAPMessage buildSOAPUserMessage(final UserMessage userMessage, MessageFragmentEntity messageFragmentEntity, final List<PartInfo> partInfoList, MessageGroupEntity messageGroupEntity) throws EbMS3Exception {
        final SOAPMessage message;
        try {
            message = xmlUtil.getMessageFactorySoap12().createMessage();
            final Ebms3Messaging ebms3Messaging = this.ebMS3Of.createMessaging();

            message.getSOAPBody().setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);

            String messageIDDigest = DigestUtils.sha256Hex(userMessage.getMessageId());
            message.getSOAPBody().addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_SOAP_BODY + messageIDDigest);
            if (userMessage.getTimestamp() == null) {
                userMessage.setTimestamp(new Date());
            }
            LOG.debug("Building SOAP User Message by attaching PartInfo and message to the Payload..");
            for (final PartInfo partInfo : partInfoList) {
                this.attachPayload(partInfo, message);
            }
            if (messageGroupEntity != null) {
                final Ebms3MessageFragmentType messageFragment = createMessageFragment(userMessage, messageFragmentEntity, partInfoList, messageGroupEntity);
                jaxbContextMessageFragment.createMarshaller().marshal(messageFragment, message.getSOAPHeader());

                final SOAPElement messageFragmentElement = (SOAPElement) message.getSOAPHeader().getChildElements(eu.domibus.api.ebms3.model.mf.ObjectFactory._MessageFragment_QNAME).next();
                messageFragmentElement.setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);
                messageFragmentElement.addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_MESSAGE_FRAGMENT + messageIDDigest);

                UserMessage cloneUserMessageFragment = userMessageFactory.cloneUserMessageFragment(userMessage);
                Ebms3UserMessage ebms3UserMessageFragment = ebms3Converter.convertToEbms3(cloneUserMessageFragment);
                ebms3Messaging.setUserMessage(ebms3UserMessageFragment);
            } else {
                Ebms3UserMessage ebms3UserMessageFragment = ebms3Converter.convertToEbms3(userMessage);
                ebms3Messaging.setUserMessage(ebms3UserMessageFragment);
            }

            this.jaxbContext.createMarshaller().marshal(ebms3Messaging, message.getSOAPHeader());

            final SOAPElement messagingElement = (SOAPElement) message.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
            messagingElement.setAttributeNS(NonRepudiationConstants.ID_NAMESPACE_URI, NonRepudiationConstants.ID_QUALIFIED_NAME, NonRepudiationConstants.URI_WSU_NS);
            messagingElement.addAttribute(NonRepudiationConstants.ID_QNAME, ID_PREFIX_MESSAGING + messageIDDigest);


            message.saveChanges();
        } catch (final SAXParseException e) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "Payload in body must be valid XML", userMessage.getMessageId(), e);
        } catch (final JAXBException | SOAPException | ParserConfigurationException | IOException | SAXException ex) {
            throw new SendMessageException(ex);
        }
        return message;
    }

    protected SOAPMessage buildSOAPMessage(final SignalMessage signalMessage) {
        final SOAPMessage message;
        try {
            message = xmlUtil.getMessageFactorySoap12().createMessage();
            final Ebms3Messaging ebms3Messaging = this.ebMS3Of.createMessaging();

            if (signalMessage != null) {
                String messageId = this.messageIdGenerator.generateMessageId();
                signalMessage.setSignalMessageId(messageId);
                signalMessage.setTimestamp(new Date());

                //Errors are not saved in the database and associated to the Signal Message
                /*if (signalMessage.getError() != null
                        && signalMessage.getError().iterator().hasNext()) {
                    signalMessage.getMessageInfo().setRefToMessageId(signalMessage.getError().iterator().next().getRefToMessageInError());
                }*/
                Ebms3SignalMessage ebms3SignalMessage = ebms3Converter.convertToEbms3(signalMessage);
                ebms3Messaging.setSignalMessage(ebms3SignalMessage);
            }
            this.jaxbContext.createMarshaller().marshal(ebms3Messaging, message.getSOAPHeader());
            message.saveChanges();
        } catch (final JAXBException | SOAPException ex) {
            throw new SendMessageException(ex);
        }

        return message;
    }

    private void attachPayload(final PartInfo partInfo, final SOAPMessage message) throws ParserConfigurationException, SOAPException, IOException, SAXException {
        String mimeType = null;

        if (partInfo.getPartProperties() != null) {
            for (final Property prop : partInfo.getPartProperties()) {
                if (Property.MIME_TYPE.equalsIgnoreCase(prop.getName())) {
                    mimeType = prop.getValue();
                }
            }
        }
        final DataHandler dataHandler = partInfo.getPayloadDatahandler();
        if (partInfo.isInBody() && mimeType != null && mimeType.toLowerCase().contains("xml")) { //TODO: respect empty soap body config
            final DocumentBuilderFactory documentBuilderFactory = xmlUtil.getDocumentBuilderFactoryNamespaceAware();
            final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            message.getSOAPBody().addDocument(builder.parse(dataHandler.getInputStream()));
            partInfo.setHref(null);
            return;
        }
        final AttachmentPart attachmentPart = message.createAttachmentPart(dataHandler);
        String href = partInfo.getHref();
        LOG.debug("Attaching Payload with PartInfo href: [{}] ", href);
        if (href != null) {
            if (href.contains("cid:")) {
                href = href.substring(href.lastIndexOf("cid:") + "cid:".length());
            }

            if (!href.startsWith("<")) {
                href = "<" + href + ">";
            }
        }
        attachmentPart.setContentId(href);
        attachmentPart.setContentType(partInfo.getMime());
        message.addAttachmentPart(attachmentPart);
    }

    public void setJaxbContext(final JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    protected Ebms3MessageFragmentType createMessageFragment(UserMessage userMessageFragment, MessageFragmentEntity messageFragmentEntity, final List<PartInfo> partInfoList, MessageGroupEntity messageGroupEntity) {
        Ebms3MessageFragmentType result = new Ebms3MessageFragmentType();

        result.setAction(messageGroupEntity.getSoapAction());

        final BigInteger compressedMessageSize = messageGroupEntity.getCompressedMessageSize();
        if (compressedMessageSize != null) {
            result.setCompressedMessageSize(compressedMessageSize);
        }
        final BigInteger messageSize = messageGroupEntity.getMessageSize();
        if (messageSize != null) {
            result.setMessageSize(messageSize);
        }
        result.setCompressionAlgorithm(messageGroupEntity.getCompressionAlgorithm());
        result.setFragmentCount(messageGroupEntity.getFragmentCount());
        result.setFragmentNum(messageFragmentEntity.getFragmentNumber());
        result.setGroupId(messageGroupEntity.getGroupId());
        result.setMustUnderstand(true);

        result.setMessageHeader(createMessageHeaderType(messageGroupEntity.getMessageHeaderEntity()));
        final PartInfo partInfo = partInfoList.iterator().next();
        result.setHref(partInfo.getHref());

        return result;
    }

    protected Ebms3MessageHeaderType createMessageHeaderType(MessageHeaderEntity messageHeaderEntity) {
        Ebms3MessageHeaderType messageHeader = new Ebms3MessageHeaderType();
        messageHeader.setBoundary(messageHeaderEntity.getBoundary());
        messageHeader.setStart(messageHeaderEntity.getStart());
        messageHeader.setContentType("Multipart/Related");
        messageHeader.setType(Ebms3TypeType.TEXT_XML);
        return messageHeader;
    }
}
