package eu.domibus.core.proxy;

import eu.domibus.api.cxf.ProxyCxfUtilService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.stereotype.Component;

/**
 * @author idragusa
 */
@Component
public class ProxyCxfUtil implements ProxyCxfUtilService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProxyCxfUtil.class);

    final DomibusProxyService domibusProxyService;

    public ProxyCxfUtil(DomibusProxyService domibusProxyService) {
        this.domibusProxyService = domibusProxyService;
    }

    @Override
    public void configureProxy(final HTTPClientPolicy httpClientPolicy, HTTPConduit httpConduit) {
        if (BooleanUtils.isNotTrue(domibusProxyService.useProxy())) {
            LOG.debug("Usage of proxy not required");
            return;
        }

        DomibusProxy proxy = domibusProxyService.getDomibusProxy();
        LOG.debug("Configuring proxy [{}] [{}] [{}] [{}] ", proxy.getHttpProxyHost(),
                proxy.getHttpProxyPort(), proxy.getHttpProxyUser(), proxy.getNonProxyHosts());
        httpClientPolicy.setProxyServer(proxy.getHttpProxyHost());
        httpClientPolicy.setProxyServerPort(proxy.getHttpProxyPort());
        httpClientPolicy.setProxyServerType(org.apache.cxf.transports.http.configuration.ProxyServerType.HTTP);

        if (!StringUtils.isBlank(proxy.getNonProxyHosts())) {
            httpClientPolicy.setNonProxyHosts(proxy.getNonProxyHosts());
        }

        if (BooleanUtils.isTrue(domibusProxyService.isProxyUserSet())) {
            ProxyAuthorizationPolicy policy = new ProxyAuthorizationPolicy();
            policy.setUserName(proxy.getHttpProxyUser());
            policy.setPassword(proxy.getHttpProxyPassword());
            httpConduit.setProxyAuthorization(policy);
        }
    }
}
