package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of dynamicdiscovery related properties
 */
@Service
public class DynamicDiscoveryEndpointChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_SMLZONE)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION)
                || StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT);
    }
}
