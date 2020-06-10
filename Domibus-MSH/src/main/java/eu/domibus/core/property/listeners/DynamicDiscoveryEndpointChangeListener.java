package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.common.services.DomibusCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of dynamicdiscovery related properties
 */
@Service
public class DynamicDiscoveryEndpointChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName,
                DOMIBUS_SMLZONE,
                DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE,
                DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION,
                DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION,
                DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT);
    }
}
