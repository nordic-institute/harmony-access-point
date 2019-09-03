package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Responsible with getting the domibus properties that can be changed at runtime, getting and setting their values
 */

@Service
public class DomibusPropertyManagerImpl implements DomibusPropertyManager {

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Autowired
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    /**
     * Returns the properties that this PropertyProvider is able to handle.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return domibusPropertyMetadataManager.getKnownProperties();
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return domibusPropertyMetadataManager.hasKnownProperty(name);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain domain = domainCode == null ? null : this.domainService.getDomain(domainCode);

        if (!meta.isDomainSpecific()) {
            return domibusPropertyProvider.getProperty(meta.getName());
        } else {
            if (meta.isWithFallback()) {
                return domibusPropertyProvider.getDomainProperty(domain, meta.getName());
            } else if (!meta.isWithFallback()) {
                return domibusPropertyProvider.getProperty(domain, meta.getName());
            }
        }

        throw new NotImplementedException("Get value for : " + propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        DomibusPropertyMetadata propMeta = this.getKnownProperties().get(propertyName);
        if (propMeta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain propertyDomain = null;
        if (domibusConfigurationService.isMultiTenantAware()) {
            propertyDomain = domainCode == null ? null : domainService.getDomain(domainCode);
            propertyDomain = propMeta.isDomainSpecific() ? propertyDomain : null;
        }
        this.domibusPropertyProvider.setPropertyValue(propertyDomain, propertyName, propertyValue);

        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }
}
