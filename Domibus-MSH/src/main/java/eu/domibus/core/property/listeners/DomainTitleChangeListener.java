package eu.domibus.core.property.listeners;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.cache.DomibusCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of domain title property
 */
@Service
public class DomainTitleChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private DomainService domainService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMAIN_TITLE);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        this.domainService.resetDomains();
    }
}
