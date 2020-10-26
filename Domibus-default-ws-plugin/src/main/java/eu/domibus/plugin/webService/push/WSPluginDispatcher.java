
package eu.domibus.plugin.webService.push;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.ProxyUtilExtService;
import eu.domibus.ext.services.TLSReaderExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.exception.WSPluginException;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import eu.domibus.webservice.backend.generated.ObjectFactory;
import eu.domibus.webservice.backend.generated.SendSuccess;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Executor;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;


/**
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service
public class WSPluginDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatcher.class);

    public static final String NAME_SPACE_DOMIBUS = "eu.europa.ec.eu.edelivery.domibus";
    public static final QName SERVICE_NAME = new QName(NAME_SPACE_DOMIBUS, "wsplugin-dispatch-service");
    public static final QName PORT_NAME = new QName(NAME_SPACE_DOMIBUS, "wsplugin-dispatch");
    private final Executor executor;
    private final JAXBContext jaxbContextWebserviceBackend;
    private final DomainContextExtService domainContextExtService;
    private final TLSReaderExtService tlsReaderDelegate;
    private final ProxyUtilExtService proxyUtilExtService;
    private final WSPluginPropertyManager wsPluginPropertyManager;

    private static final ThreadLocal<MessageFactory> messageFactoryThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (SOAPException e) {
            throw new WSPluginException("Error initializing MessageFactory", e);
        }
    });

    public WSPluginDispatcher(@Qualifier("taskExecutor") Executor executor,
                              @Qualifier(value = "jaxbContextWebserviceBackend") JAXBContext jaxbContextWebserviceBackend,
                              DomainContextExtService domainContextExtService,
                              TLSReaderExtService tlsReaderDelegate,
                              ProxyUtilExtService proxyUtilExtService,
                              WSPluginPropertyManager wsPluginPropertyManager) {
        this.executor = executor;
        this.jaxbContextWebserviceBackend = jaxbContextWebserviceBackend;
        this.domainContextExtService = domainContextExtService;
        this.tlsReaderDelegate = tlsReaderDelegate;
        this.proxyUtilExtService = proxyUtilExtService;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
    }

    @Timer(clazz = WSPluginDispatcher.class, value = "dispatch")
    @Counter(clazz = WSPluginDispatcher.class, value = "dispatch")
    public SOAPMessage dispatch(final SOAPMessage soapMessage, String endpoint) {
        DomainDTO domain = domainContextExtService.getCurrentDomain();

        final Dispatch<SOAPMessage> dispatch = getClient(domain.getCode(), endpoint);

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
        SOAPMessage message = messageFactoryThreadLocal.get().createMessage();

        SendSuccess sendSuccess = new ObjectFactory().createSendSuccess();
        sendSuccess.setMessageID(messageId);
        jaxbContextWebserviceBackend.createMarshaller().marshal(sendSuccess, message.getSOAPBody());

        LOG.info(getXML(message));
        return message;
    }

    public String getXML(SOAPMessage message) throws SOAPException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        return new String(out.toByteArray());
    }

    public Dispatch<SOAPMessage> getClient(String domain, String endpoint) {
        LOG.debug("Getting the dispatch client for ws plugin endpoint [{}] on domain [{}]", endpoint, domain);

        final Dispatch<SOAPMessage> dispatch = createWSServiceDispatcher(endpoint);
        final Client client = ((DispatchImpl<SOAPMessage>) dispatch).getClient();
        final HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        final HTTPClientPolicy httpClientPolicy = httpConduit.getClient();

        httpConduit.setClient(httpClientPolicy);
        setHttpClientPolicy(httpClientPolicy);

        if (endpoint.startsWith("https://")) {
            final TLSClientParameters params = tlsReaderDelegate.getTlsClientParameters(domain);
            if (params != null) {
                httpConduit.setTlsClientParameters(params);
            }
        }

        proxyUtilExtService.configureProxy(httpClientPolicy, httpConduit);
        LOG.debug("END Getting the dispatch client for ws plugin endpoint [{}] on domain [{}]", endpoint, domain);

        return dispatch;
    }

    protected Dispatch<SOAPMessage> createWSServiceDispatcher(String endpoint) {
        final javax.xml.ws.Service service = javax.xml.ws.Service.create(SERVICE_NAME);
        service.setExecutor(executor);
        service.addPort(PORT_NAME, SOAPBinding.SOAP12HTTP_BINDING, endpoint);
        return service.createDispatch(PORT_NAME, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
    }

    public void setHttpClientPolicy(HTTPClientPolicy httpClientPolicy) {
        httpClientPolicy.setConnectionTimeout(parseInt(wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_CONNECTION_TIMEOUT)));
        httpClientPolicy.setReceiveTimeout(parseInt(wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_RECEIVE_TIMEOUT)));
        httpClientPolicy.setAllowChunking(parseBoolean(wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_ALLOW_CHUNKING)));
        httpClientPolicy.setChunkingThreshold(parseInt(wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_CHUNKING_THRESHOLD)));

        ConnectionType connectionType = ConnectionType.CLOSE;
        if (parseBoolean(wsPluginPropertyManager.getKnownPropertyValue(DISPATCHER_CONNECTION_KEEP_ALIVE))) {
            connectionType = ConnectionType.KEEP_ALIVE;
        }
        httpClientPolicy.setConnection(connectionType);
    }
}

