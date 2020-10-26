package eu.domibus.ext.delegate.services.http;

import eu.domibus.api.cxf.http.ProxyUtilService;
import eu.domibus.ext.services.ProxyUtilExtService;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 * <p>
 * Delegate class allowing Domibus extension/plugin to use TLS configuration of Domibus.
 */
@Service
public class ProxyUtilDelegate implements ProxyUtilExtService {
    private final ProxyUtilService proxyUtilService;

    public ProxyUtilDelegate(ProxyUtilService proxyUtilService) {
        this.proxyUtilService = proxyUtilService;
    }

    public void configureProxy(HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) {
        proxyUtilService.configureProxy(httpClientPolicy, httpConduit);
    }

}
