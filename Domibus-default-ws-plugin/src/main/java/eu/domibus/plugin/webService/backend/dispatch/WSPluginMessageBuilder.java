package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.PartInfo;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.connector.WSPluginImpl;
import eu.domibus.plugin.webService.exception.WSPluginException;
import eu.domibus.plugin.webService.impl.ExtendedPartInfo;
import eu.domibus.webservice.backend.generated.*;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginMessageBuilder {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageBuilder.class);

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
            case DELETED:
            case MESSAGE_STATUS_CHANGE:
            default:
                throw new IllegalArgumentException("Unexpected value: " + messageLogEntity.getType());
        }
    }

    private SubmitMessage getSubmitMessage(WSBackendMessageLogEntity messageLogEntity) {
        String messageId = messageLogEntity.getMessageId();//to be trimmed?

        UserMessage userMessage = new UserMessage();
        try {
            userMessage = wsPlugin.browseMessage(messageId, userMessage);
        } catch (MessageNotFoundException e) {
            throw new WSPluginException("Domibus message could not be found with message id: [" + messageId + "]", e);
        }
        SubmitMessage submitMessage = new eu.domibus.webservice.backend.generated.ObjectFactory().createSubmitMessage();

        fillInfoPartsForLargeFiles(submitMessage, userMessage);
        return submitMessage;
    }

    private void fillInfoPartsForLargeFiles(SubmitMessage retrieveMessageResponse, UserMessage userMessage) {
        if (userMessage.getPayloadInfo() == null || CollectionUtils.isEmpty(userMessage.getPayloadInfo().getPartInfo())) {
            LOG.info("No payload found for message [{}]", userMessage.getMessageInfo().getMessageId());
            return;
        }

        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            ExtendedPartInfo extPartInfo = (ExtendedPartInfo) partInfo;
            eu.domibus.webservice.backend.generated.LargePayloadType payloadType = new eu.domibus.webservice.backend.generated.ObjectFactory().createLargePayloadType();
            if (extPartInfo.getPayloadDatahandler() != null) {
                LOG.debug("payloadDatahandler Content Type: " + extPartInfo.getPayloadDatahandler().getContentType());
                payloadType.setValue(extPartInfo.getPayloadDatahandler());
            }
            if (extPartInfo.isInBody()) {
                retrieveMessageResponse.setBodyload(payloadType);
            } else {
                payloadType.setPayloadId(partInfo.getHref());
                retrieveMessageResponse.getPayload().add(payloadType);
            }
        }
    }

    private ReceiveFailure getReceiveFailure(WSBackendMessageLogEntity messageLogEntity) {
        ReceiveFailure sendFailure = new ObjectFactory().createReceiveFailure();
        sendFailure.setMessageID(messageLogEntity.getMessageId());
        return sendFailure;
    }

    private ReceiveSuccess getReceiveSuccess(WSBackendMessageLogEntity messageLogEntity) {
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

            this.jaxbContextWebserviceBackend.createMarshaller().marshal(messaging, message.getSOAPBody());
            message.saveChanges();
        } catch (final JAXBException | SOAPException ex) {
            throw new WSPluginException("Could not build the soap message for ws plugin", ex);
        }

        return message;
    }
}
