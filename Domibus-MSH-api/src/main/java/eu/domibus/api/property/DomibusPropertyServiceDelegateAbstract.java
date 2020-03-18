package eu.domibus.api.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Abstract class that implements DomibusPropertyManager and delegates its methods to DomibusPropertyProvider
 * Used to derive server specific property managers that delegate to Domibus property manager. Ex: TomcatPropertyManager
 */
public abstract class DomibusPropertyServiceDelegateAbstract implements DomibusPropertyManager {

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainService domainService;

    @Autowired
    DomainContextProvider domainContextService;

    public abstract Map<String, DomibusPropertyMetadata> getKnownProperties();

    @Override
    public String getKnownPropertyValue(String propertyName) {
        checkPropertyExists(propertyName);

        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName){
        checkPropertyExists(propertyName);

        final Domain domain = domainService.getDomain(domainCode);
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        checkPropertyExists(propertyName);

        final Domain domain = domainService.getDomain(domainCode);
        domibusPropertyProvider.setPropertyValue(domain, propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        checkPropertyExists(propertyName);

        Domain currentDomain = domainContextService.getCurrentDomainSafely();
        domibusPropertyProvider.setPropertyValue(currentDomain, propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        checkPropertyExists(propertyName);

        final Domain domain = domainService.getDomain(domainCode);
        domibusPropertyProvider.setPropertyValue(domain, propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }

    private void checkPropertyExists(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyException("Unknown property: " + propertyName);
        }
    }
}
