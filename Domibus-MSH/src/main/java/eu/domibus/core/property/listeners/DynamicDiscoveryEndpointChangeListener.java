package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of dynamicdiscovery related properties
 */
@Service
public class DynamicDiscoveryEndpointChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domibus.smlzone")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dynamicdiscovery.peppolclient.mode")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation")
                || StringUtils.equalsIgnoreCase(propertyName, "domibus.dynamicdiscovery.transportprofileas4");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT);
    }
}
