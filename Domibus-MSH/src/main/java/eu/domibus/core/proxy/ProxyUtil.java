package eu.domibus.core.proxy;

import eu.domibus.api.http.ProxyUtilService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author idragusa
 */
@Component
public class ProxyUtil implements ProxyUtilService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProxyUtil.class);

    @Autowired
    DomibusProxyService domibusProxyService;

    @Override
    public HttpHost getConfiguredProxy() {
        if (BooleanUtils.isTrue(domibusProxyService.useProxy())) {
            DomibusProxy domibusProxy = domibusProxyService.getDomibusProxy();
            LOG.debug("Proxy enabled, get configured proxy [{}] [{}]", domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort());
            return new HttpHost(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort());
        }
        LOG.debug("Proxy not enabled, configured proxy is null");
        return null;
    }

    @Override
    public CredentialsProvider getConfiguredCredentialsProvider() {

        if(domibusProxyService.useProxy() && domibusProxyService.isProxyUserSet()) {
            DomibusProxy domibusProxy = domibusProxyService.getDomibusProxy();
            LOG.debug("Proxy enabled, configure credentials provider for [{}]", domibusProxy.getHttpProxyUser());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(domibusProxy.getHttpProxyHost(), domibusProxy.getHttpProxyPort()),
                    new UsernamePasswordCredentials(domibusProxy.getHttpProxyUser(), domibusProxy.getHttpProxyPassword()));

            return credsProvider;
        }
        LOG.debug("Proxy not enabled, credentials provider is null");
        return null;
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
