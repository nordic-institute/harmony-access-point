package eu.domibus.plugin.webService.backend.dispatch;

import eu.domibus.ext.services.ProxyCxfUtilExtService;
import eu.domibus.ext.services.TLSReaderExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;
import java.util.concurrent.Executor;

import static eu.domibus.plugin.webService.property.WSPluginPropertyManager.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatchClientProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatchClientProvider.class);

    public static final String NAMESPACE_DOMIBUS = "eu.domibus";
    public static final QName SERVICE_NAME = new QName(NAMESPACE_DOMIBUS, "wsplugin-dispatch-service");
    public static final QName PORT_NAME = new QName(NAMESPACE_DOMIBUS, "wsplugin-dispatch");

    private final Executor executor;
    private final TLSReaderExtService tlsReaderDelegate;
    private final ProxyCxfUtilExtService proxyUtilExtService;
    private final WSPluginPropertyManager wsPluginPropertyManager;

    public WSPluginDispatchClientProvider(@Qualifier("taskExecutor") Executor executor,
                                          TLSReaderExtService tlsReaderDelegate,
                                          ProxyCxfUtilExtService proxyUtilExtService,
                                          WSPluginPropertyManager wsPluginPropertyManager) {
        this.executor = executor;
        this.tlsReaderDelegate = tlsReaderDelegate;
        this.proxyUtilExtService = proxyUtilExtService;
        this.wsPluginPropertyManager = wsPluginPropertyManager;
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
        LOG.debug("END getting the dispatch client for ws plugin endpoint [{}] on domain [{}]", endpoint, domain);

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
