package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.exception.WSPluginException;
import eu.domibus.webservice.backend.generated.ObjectFactory;
import eu.domibus.webservice.backend.generated.SendFailure;
import eu.domibus.webservice.backend.generated.SendSuccess;
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

    private final XMLUtilExtService xmlUtilExtService;

    private final JAXBContext jaxbContextWebserviceBackend;

    public WSPluginMessageBuilder(XMLUtilExtService xmlUtilExtService, JAXBContext jaxbContextWebserviceBackend) {
        this.xmlUtilExtService = xmlUtilExtService;
        this.jaxbContextWebserviceBackend = jaxbContextWebserviceBackend;
    }

    public SOAPMessage buildSOAPMessageSendSuccess(final WSBackendMessageLogEntity messageLogEntity) {
        Object jaxbElement = getJaxbElement(messageLogEntity);
        SOAPMessage soapMessage = buildSOAPMessage(jaxbElement);

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
            case RECEIVE_FAIL:
            case MESSAGE_STATUS_CHANGE:
            case SUBMIT_MESSAGE:
            default:
                throw new IllegalArgumentException("Unexpected value: " + messageLogEntity.getType());
        }
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

    protected SOAPMessage buildSOAPMessage(final Object messaging) {
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
