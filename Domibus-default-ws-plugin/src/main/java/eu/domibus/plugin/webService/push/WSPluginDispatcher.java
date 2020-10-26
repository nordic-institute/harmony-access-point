
package eu.domibus.plugin.webService.push;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.exception.WSPluginException;
import eu.domibus.webservice.backend.generated.ObjectFactory;
import eu.domibus.webservice.backend.generated.SendSuccess;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;

import static eu.domibus.plugin.webService.configuration.WSPluginConfiguration.JAXB_CONTEXT_WEBSERVICE_BACKEND;


/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatcher.class);
    private final JAXBContext jaxbContextWebserviceBackend;
    private final DomainContextExtService domainContextExtService;
    private final XMLUtilExtService xmlUtilExtService;
    private final WSPluginDispatchClientProvider wsPluginDispatchClientProvider;


    public WSPluginDispatcher(@Qualifier(value = JAXB_CONTEXT_WEBSERVICE_BACKEND) JAXBContext jaxbContextWebserviceBackend,
                              DomainContextExtService domainContextExtService,
                              XMLUtilExtService xmlUtilExtService,
                              WSPluginDispatchClientProvider wsPluginDispatchClientProvider) {
        this.jaxbContextWebserviceBackend = jaxbContextWebserviceBackend;
        this.domainContextExtService = domainContextExtService;
        this.xmlUtilExtService = xmlUtilExtService;
        this.wsPluginDispatchClientProvider = wsPluginDispatchClientProvider;
    }

    @Timer(clazz = WSPluginDispatcher.class, value = "dispatch")
    @Counter(clazz = WSPluginDispatcher.class, value = "dispatch")
    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint) {
        DomainDTO domain = domainContextExtService.getCurrentDomain();

        final Dispatch<SOAPMessage> dispatch = wsPluginDispatchClientProvider.getClient(domain.getCode(), endpoint);

        final SOAPMessage result;
        try {
            result = dispatch.invoke(soapMessage);
        } catch (final WebServiceException e) {
            Exception exception = e;
            if (e.getCause() instanceof ConnectException) {
                exception = new WebServiceException("Error dispatching message to [" + endpoint + "]: possible reason is that the receiver is not available", e);
            }
            throw new WSPluginException("Error dispatching message to " + endpoint, exception);
        }
        return result;
    }

    public SOAPMessage getSoapMessageSendSuccess(String messageId) throws SOAPException, JAXBException, IOException {
        SOAPMessage message = xmlUtilExtService.getMessageFactorySoap12().createMessage();

        SendSuccess sendSuccess = new ObjectFactory().createSendSuccess();
        sendSuccess.setMessageID(messageId);
        jaxbContextWebserviceBackend.createMarshaller().marshal(sendSuccess, message.getSOAPBody());

        LOG.debug("Getting message for send succes: [{}]", getXML(message));
        return message;
    }

    public String getXML(SOAPMessage message) throws SOAPException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        return new String(out.toByteArray());
    }
}

