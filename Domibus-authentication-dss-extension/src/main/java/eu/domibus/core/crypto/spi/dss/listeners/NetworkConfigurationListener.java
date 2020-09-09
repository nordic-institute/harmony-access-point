package eu.domibus.core.crypto.spi.dss.listeners;

import com.google.common.collect.Sets;
import eu.domibus.core.crypto.spi.dss.DomibusDataLoader;
import eu.domibus.core.crypto.spi.dss.ProxyHelper;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;

import java.util.Set;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * Handles proxy configuration change.
 *
 * @author Thomas Dussart
 * @since 4.2
 */
public class NetworkConfigurationListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NetworkConfigurationListener.class);

    private final Set<String> properties = Sets.newHashSet(
            AUTHENTICATION_DSS_PROXY_HTTP_HOST,
            AUTHENTICATION_DSS_PROXY_HTTP_PORT,
            AUTHENTICATION_DSS_PROXY_HTTP_USER,
            AUTHENTICATION_DSS_PROXY_HTTP_PASSWORD,
            AUTHENTICATION_DSS_PROXY_HTTP_EXCLUDEDHOSTS,
            AUTHENTICATION_DSS_PROXY_HTTPS_HOST,
            AUTHENTICATION_DSS_PROXY_HTTPS_PORT,
            AUTHENTICATION_DSS_PROXY_HTTPS_USER,
            AUTHENTICATION_DSS_PROXY_HTTPS_PASSWORD,
            AUTHENTICATION_DSS_PROXY_HTTPS_EXCLUDEDHOSTS);

    private DomibusDataLoader dataLoader;

    private ProxyHelper proxyHelper;

    public NetworkConfigurationListener(DomibusDataLoader dataLoader, ProxyHelper proxyHelper) {
        this.dataLoader = dataLoader;
        this.proxyHelper = proxyHelper;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean matchingProperty = properties.contains(propertyName);
        if(matchingProperty){
            LOG.info("Dss Property:[{}] changed",propertyName);
        }
        return matchingProperty;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.info("Reloading proxy configuration");
        dataLoader.setProxyConfig(proxyHelper.getProxyConfig());
    }
}
