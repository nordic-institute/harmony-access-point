package eu.domibus.core.crypto.spi.dss.listeners;

import com.google.common.collect.Sets;
import eu.domibus.core.crypto.spi.dss.DssCache;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;

import java.util.Set;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class CertificateVerifierListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NetworkConfigurationListener.class);

    private final Set<String> properties = Sets.newHashSet(
            DSS_PERFORM_CRL_CHECK,
            AUTHENTICATION_DSS_CHECK_REVOCATION_FOR_UNTRUSTED_CHAINS,
            AUTHENTICATION_DSS_EXCEPTION_ON_MISSING_REVOCATION_DATA);

    private final DssCache dssCache;

    public CertificateVerifierListener(DssCache dssCache) {
        this.dssCache = dssCache;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean matchingProperty = properties.contains(propertyName);
        if(matchingProperty){
            LOG.info("Property:[{}] changed",propertyName);
        }
        return matchingProperty;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {
        LOG.info("Reloading proxy configuration");
        dssCache.clear();
    }

}
