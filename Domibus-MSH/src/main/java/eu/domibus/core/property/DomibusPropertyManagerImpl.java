package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
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

    @Autowired
    protected DomainContextProvider domainContextProvider;

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
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException(propertyName);
        }
        Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException(propertyName);
        }

        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        Domain domain = domainCode != null ? domainService.getDomain(domainCode) : null;
        this.setPropertyValue(domain, propertyName, propertyValue, true);

//        DomibusPropertyMetadata propMeta = this.getKnownProperties().get(propertyName);
//        if (propMeta == null) {
//            throw new IllegalArgumentException(propertyName);
//        }
//
//        Domain propertyDomain = null;
//        if (domibusConfigurationService.isMultiTenantAware()) {
//            propertyDomain = domainCode == null ? null : domainService.getDomain(domainCode);
//            propertyDomain = propMeta.isGlobal() ? null : propertyDomain;
//        }
//        domibusPropertyProvider.setPropertyValue(propertyDomain, propertyName, propertyValue);
//
//        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
//        propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        this.setPropertyValue(domain, propertyName, propertyValue, true);
    }

    private void setPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        DomibusPropertyMetadata propMeta = this.getKnownProperties().get(propertyName);
        if (propMeta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        domibusPropertyProvider.setPropertyValue(domain, propertyName, propertyValue);

        String domainCode = domain != null ? domain.getCode() : null;
        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
    }
}
