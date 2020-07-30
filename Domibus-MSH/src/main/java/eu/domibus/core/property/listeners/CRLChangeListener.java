package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS;

/**
 * @author Soumya Chandran
 * @since 4.2
 * <p>
 * Handles the change of property to exclude protocols from CRL list
 */
@Service
public class CRLChangeListener implements DomibusPropertyChangeListener {

    private DomibusCacheService domibusCacheService;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLChangeListener.class);

    public CRLChangeListener(DomibusCacheService domibusCacheService) {
        this.domibusCacheService = domibusCacheService;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.trace("Clearing cache for property [{}]", propertyName);
        domibusCacheService.clearCache(DomibusCacheService.CRL_BY_CERT);
    }
}
