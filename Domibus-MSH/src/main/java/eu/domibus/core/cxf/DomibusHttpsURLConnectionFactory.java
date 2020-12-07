package eu.domibus.core.cxf;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.https.HttpsURLConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * A Domibus factory providing the creation of custom {@code HttpURLConnection} objects, that are ignoring any TLS
 * parameters present in CXF.
 *
 * <p>Note: this factory should only be used when offloading the SSL to another responsible application (e.g. a forward
 * SSL proxy).</p>
 *
 * @author Sestian-Ion TINCU
 */
@Component
public class DomibusHttpsURLConnectionFactory extends HttpsURLConnectionFactory {

    @Override
    public HttpURLConnection createConnection(TLSClientParameters tlsClientParameters,
                                              Proxy proxy, URL url) throws IOException {
        return (HttpURLConnection) (proxy != null
                        ? url.openConnection(proxy)
                        : url.openConnection());
    }
}
