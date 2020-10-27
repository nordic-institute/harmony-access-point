package eu.domibus.plugin.webService.push;

import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.exception.WSPluginException;
import eu.domibus.webservice.backend.generated.ObjectFactory;
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

    public SOAPMessage buildSOAPMessageSendSuccess(final String messageId) {
        SendSuccess sendSuccess = new ObjectFactory().createSendSuccess();
        sendSuccess.setMessageID(messageId);
        SOAPMessage soapMessage = buildSOAPMessage(sendSuccess);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting message for class [{}]: [{}]", sendSuccess.getClass(), getXML(soapMessage));
        }
        return soapMessage;
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
