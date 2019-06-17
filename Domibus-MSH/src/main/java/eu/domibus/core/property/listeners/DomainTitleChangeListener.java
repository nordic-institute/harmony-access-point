package eu.domibus.core.property.listeners;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.property.DomibusPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DomainTitleChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomibusCacheService domibusCacheService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, "domain.title");
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domibusCacheService.clearCache(DomibusCacheService.ALL_DOMAINS_CACHE);
    }
}
