package eu.domibus.core.cxf;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.https.HttpsURLConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE;

/**
 * A Domibus factory providing the creation of custom {@code HttpURLConnection} objects that support SSL offloading by
 * ignoring any TLS parameters present in CXF.
 *
 * @author Sestian-Ion TINCU
 * @since 5.0
 */
@Component
public class DomibusHttpsURLConnectionFactory extends HttpsURLConnectionFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusHttpsURLConnectionFactory.class);

    private final DomibusPropertyProvider domibusPropertyProvider;

    public DomibusHttpsURLConnectionFactory(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public HttpURLConnection createConnection(TLSClientParameters tlsClientParameters,
                                              Proxy proxy, URL url) throws IOException {
        Boolean sslOffload = domibusPropertyProvider.getBooleanProperty(DOMIBUS_CONNECTION_CXF_SSL_OFFLOAD_ENABLE);
        if(BooleanUtils.isTrue(sslOffload)) {
            LOG.debug("Configure the HTTP connection for SSL offloading, ignoring any existing TLS client parameters: proxy=[{}]", proxy);
            return (HttpURLConnection) (proxy != null
                            ? url.openConnection(proxy)
                            : url.openConnection());
        }
        return super.createConnection(tlsClientParameters, proxy, url);
    }
}
