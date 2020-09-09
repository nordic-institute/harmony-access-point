package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.esig.dss.service.http.proxy.ProxyConfig;
import eu.europa.esig.dss.service.http.proxy.ProxyProperties;
import org.apache.commons.lang3.StringUtils;

/**
 * Dss proxy configuration class.
 *
 * @author Thomas Dussart
 * @since 4.2
 */
public class ProxyHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ProxyHelper.class);

    private DssExtensionPropertyManager dssExtensionPropertyManager;

    public ProxyHelper(DssExtensionPropertyManager dssExtensionPropertyManager) {
        this.dssExtensionPropertyManager = dssExtensionPropertyManager;
    }

    public ProxyConfig getProxyConfig() {
        ProxyConfig proxyConfig = new ProxyConfig();

        String httpHost = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_HOST);
        String httpPort = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_PORT);
        String httpUser = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_USER);
        String httpPassword = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD);
        String httpExcludedHost = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS);

        LOG.info("Http proxy configuration: host[{}], port:[{}], user:[{}], excluded host:[{}]",httpHost,httpPort,httpUser,httpExcludedHost);
        ProxyProperties httpProxyProperties = prepareProxyProperties(httpHost, httpPort, httpUser, httpPassword, httpExcludedHost);
        if (httpProxyProperties != null) {
            proxyConfig.setHttpProperties(httpProxyProperties);
        }

        String httpsHost = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_HOST);
        String httpsPort = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_PORT);
        String httpsUser = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_USER);
        String httpsPassword = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD);
        String httpsExcludedHost = dssExtensionPropertyManager.getKnownPropertyValue(DssExtensionPropertyManager.AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS);

        LOG.info("Https proxy configuration: host[{}], port:[{}], user:[{}], excluded host:[{}]",httpsHost,httpsPort,httpsUser,httpsExcludedHost);

        ProxyProperties httpsProxyProperties = prepareProxyProperties(httpsHost, httpsPort, httpsUser, httpsPassword, httpsExcludedHost);
        if (httpsProxyProperties != null) {
            proxyConfig.setHttpsProperties(httpsProxyProperties);
        }
        return proxyConfig;
    }

    private ProxyProperties prepareProxyProperties(final String proxyHost, final String proxyPort, final String proxyUser, final String proxyPassword, final String proxyExcludedHosts) {
        if (StringUtils.isNotEmpty(proxyHost)) {
            try {
                int port = Integer.parseInt(proxyPort);
                return getProxyProperties(proxyHost, port, proxyUser, proxyPassword, proxyExcludedHosts);
            } catch (NumberFormatException n) {
                LOG.warn("Error parsing https port config:[{}], skipping:[{}] https configuration", proxyPort, proxyHost, n);
            }
        }
        return null;
    }

    private ProxyProperties getProxyProperties(final String host,
                                               final int port,
                                               final String user,
                                               final String password,
                                               final String excludedHosts) {

        LOG.debug("Using proxy properties host:[{}],port:[{}],user:[{}],excludedHosts:[{}]", host, port, user, excludedHosts);
        final ProxyProperties httpsProperties = new ProxyProperties();
        httpsProperties.setHost(host);
        httpsProperties.setPort(port);
        httpsProperties.setUser(user);
        httpsProperties.setPassword(password);
        httpsProperties.setExcludedHosts(excludedHosts);
        return httpsProperties;
    }
}
