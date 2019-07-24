package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMAIN_TITLE;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Handles the change of domain title propertiy
 */
@Service
public class DomainTitleChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMAIN_TITLE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.ALL_DOMAINS_CACHE);
    }
}
