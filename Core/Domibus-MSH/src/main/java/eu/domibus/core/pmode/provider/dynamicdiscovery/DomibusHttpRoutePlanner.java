package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.core.ssl.offload.SslOffloadService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * A Domibus route planner that allows switching the SSL connections to HTTP for SSL offloading scenarios.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Service
public class DomibusHttpRoutePlanner implements HttpRoutePlanner {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusHttpRoutePlanner.class);

    private final SslOffloadService sslOffloadService;
    private DefaultRoutePlanner routePlanner;

    public DomibusHttpRoutePlanner(SslOffloadService sslOffloadService) {
        this.sslOffloadService = sslOffloadService;
        routePlanner = new DefaultRoutePlanner(null);
    }

    @Override
    public HttpRoute determineRoute(HttpHost host, HttpContext context) throws HttpException {
        HttpRoute route = routePlanner.determineRoute(host, context);

        URL url = getUrl(host);

        if (sslOffloadService.isSslOffloadEnabled(url)) {
            LOG.info("Switch [{}] to an HTTP connection for SSL offloading", host);
            route = route.getProxyHost() == null
                    ? new HttpRoute(route.getTargetHost(), route.getLocalAddress(), false)
                    : new HttpRoute(route.getTargetHost(), route.getLocalAddress(), route.getProxyHost(), false);
        }
        return route;
    }

    private URL getUrl(HttpHost host) {
        URL url = null;
        try {
            LOG.debug("Convert the host String representation [{}] to a URL", host);
            url = new URL(host.toURI());
        } catch (Exception e) {
            LOG.error("An error occurred when creating a URL from the host String representation", e);
        }
        return url;
    }
}
