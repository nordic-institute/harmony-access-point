package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.exception.WSPluginException;
import eu.domibus.plugin.ws.webservice.ExtendedPartInfo;
import eu.domibus.webservice.backend.generated.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageBuilder.class);
    public static final String MIME_TYPE = "MimeType";
    public static final String PAYLOAD_NAME = "PayloadName";

    private final JAXBContext jaxbContextWebserviceBackend;

    private final XMLUtilExtService xmlUtilExtService;

    private final WSPluginImpl wsPlugin;

    public WSPluginMessageBuilder(XMLUtilExtService xmlUtilExtService,
                                  JAXBContext jaxbContextWebserviceBackend,
                                  WSPluginImpl wsPlugin) {
        this.xmlUtilExtService = xmlUtilExtService;
        this.jaxbContextWebserviceBackend = jaxbContextWebserviceBackend;
        this.wsPlugin = wsPlugin;
    }

    public SOAPMessage buildSOAPMessage(final WSBackendMessageLogEntity messageLogEntity) {
        Object jaxbElement = getJaxbElement(messageLogEntity);
        SOAPMessage soapMessage = createSOAPMessage(jaxbElement);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting message for class [{}]: [{}]", jaxbElement.getClass(), getXML(soapMessage));
        }
        return soapMessage;
    }

    protected Object getJaxbElement(WSBackendMessageLogEntity messageLogEntity) {
        switch (messageLogEntity.getType()) {
            case SEND_SUCCESS:
                return getSendSuccess(messageLogEntity);
            case SEND_FAILURE:
                return getSendFailure(messageLogEntity);
            case RECEIVE_SUCCESS:
                return getReceiveSuccess(messageLogEntity);
            case RECEIVE_FAIL:
                return getReceiveFailure(messageLogEntity);
            case SUBMIT_MESSAGE:
                return getSubmitMessage(messageLogEntity);
            case DELETED_BATCH:
                return getDeleteBatch(messageLogEntity);
            case DELETED:
                return getDelete(messageLogEntity);
            case MESSAGE_STATUS_CHANGE:
            default:
                throw new IllegalArgumentException("Unexpected value: " + messageLogEntity.getType());
        }
    }

    protected SubmitMessage getSubmitMessage(WSBackendMessageLogEntity messageLogEntity) {
        String messageId = messageLogEntity.getMessageId();

        UserMessage userMessage = new UserMessage();
        try {
            userMessage = wsPlugin.browseMessage(messageId, userMessage);
        } catch (MessageNotFoundException e) {
            throw new WSPluginException("Domibus message could not be found with message id: [" + messageId + "]", e);
        }
        SubmitMessage submitMessage = new eu.domibus.webservice.backend.generated.ObjectFactory().createSubmitMessage();
        submitMessage.setMessageID(messageLogEntity.getMessageId());
        submitMessage.setFinalRecipient(messageLogEntity.getFinalRecipient());
        submitMessage.setOriginalSender(messageLogEntity.getOriginalSender());

        fillInfoPartsForLargeFiles(submitMessage, userMessage);
        return submitMessage;
    }

    protected void fillInfoPartsForLargeFiles(SubmitMessage submitMessage, UserMessage userMessage) {
        if (userMessage.getPayloadInfo() == null || CollectionUtils.isEmpty(userMessage.getPayloadInfo().getPartInfo())) {
            String messageId;
            if (userMessage.getMessageInfo() != null) {
                messageId = userMessage.getMessageInfo().getMessageId();
                LOG.info("No payload found for message [{}]", messageId);
            }
            return;
        }

        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            fillInfoPart(submitMessage, (ExtendedPartInfo) partInfo);
        }
    }

    protected void fillInfoPart(SubmitMessage submitMessage, ExtendedPartInfo extPartInfo) {
        eu.domibus.webservice.backend.generated.LargePayloadType payloadType = new eu.domibus.webservice.backend.generated.ObjectFactory().createLargePayloadType();
        if (extPartInfo.getPayloadDatahandler() != null) {
            LOG.debug("payloadDatahandler Content Type: {}", extPartInfo.getPayloadDatahandler().getContentType());
            payloadType.setValue(extPartInfo.getPayloadDatahandler());
        }
        payloadType.setMimeType(getAnyPropertyValue(extPartInfo, MIME_TYPE));
        payloadType.setPayloadName(getAnyPropertyValue(extPartInfo, PAYLOAD_NAME));

        if (extPartInfo.isInBody()) {
            submitMessage.setBodyload(payloadType);
        } else {
            payloadType.setPayloadId(extPartInfo.getHref());
            submitMessage.getPayload().add(payloadType);
        }
    }

    protected String getAnyPropertyValue(ExtendedPartInfo extPartInfo, String mimeType) {
        return extPartInfo
                .getPartProperties()
                .getProperty()
                .stream()
                .filter(property -> equalsAnyIgnoreCase(mimeType, property.getName()))
                .findAny()
                .map(Property::getValue)
                .orElse(null);
    }

    protected ReceiveFailure getReceiveFailure(WSBackendMessageLogEntity messageLogEntity) {
        ReceiveFailure sendFailure = new ObjectFactory().createReceiveFailure();
        sendFailure.setMessageID(messageLogEntity.getMessageId());
        return sendFailure;
    }

    protected ReceiveSuccess getReceiveSuccess(WSBackendMessageLogEntity messageLogEntity) {
        ReceiveSuccess sendFailure = new ObjectFactory().createReceiveSuccess();
        sendFailure.setMessageID(messageLogEntity.getMessageId());
        return sendFailure;
    }

    protected SendFailure getSendFailure(WSBackendMessageLogEntity messageLogEntity) {
        SendFailure sendFailure = new ObjectFactory().createSendFailure();
        sendFailure.setMessageID(messageLogEntity.getMessageId());
        return sendFailure;
    }

    protected SendSuccess getSendSuccess(WSBackendMessageLogEntity messageLogEntity) {
        SendSuccess sendSuccess = new ObjectFactory().createSendSuccess();
        sendSuccess.setMessageID(messageLogEntity.getMessageId());
        return sendSuccess;
    }

    protected Delete getDelete(WSBackendMessageLogEntity messageLogEntity) {
        Delete delete = new ObjectFactory().createDelete();
        delete.setMessageID(messageLogEntity.getMessageId());
        return delete;
    }

    protected DeleteBatch getDeleteBatch(WSBackendMessageLogEntity messageLogEntity) {
        DeleteBatch deleteBatch = new ObjectFactory().createDeleteBatch();
        String[] split = StringUtils.split(messageLogEntity.getMessageId(), ";");
        if (split == null || split.length == 0) {
            throw new WSPluginException("DELETE_BATCH cannot be send because no message ids found");
        }
        deleteBatch.getMessageIds().addAll(Arrays.stream(split).collect(Collectors.toList()));
        return deleteBatch;
    }

    public String getXML(SOAPMessage message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            message.writeTo(out);
            return new String(out.toByteArray());
        } catch (SOAPException | IOException e) {
            return "Could not read the soap message for ws plugin";
        }
    }

    protected SOAPMessage createSOAPMessage(final Object messaging) {
        final SOAPMessage message;
        try {
            message = xmlUtilExtService.getMessageFactorySoap12().createMessage();

            jaxbContextWebserviceBackend.createMarshaller().marshal(messaging, message.getSOAPBody());
            message.saveChanges();
        } catch (final JAXBException | SOAPException ex) {
            throw new WSPluginException("Could not build the soap message for ws plugin", ex);
        }

        return message;
    }
}
