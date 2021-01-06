package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Single entry point for getting and setting internal and external domibus properties
 *
 * @author Cosmin Baciu, Ion Perpegel
 * @since 4.0
 */
@Service
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    protected DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Autowired
    protected PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Autowired
    DomibusNestedPropertiesManager domibusNestedPropertiesManager;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    DomibusPropertyProviderHelper domibusPropertyProviderHelper;

    @Override
    public String getProperty(String propertyName) throws DomibusPropertyException {
        return domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
    }

    @Override
    public String getProperty(Domain domain, String propertyName) throws DomibusPropertyException {
        if (domain == null) {
            throw new DomibusPropertyException("Property " + propertyName + " cannot be retrieved without a domain");
        }
        return domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getIntegerInternal(propertyName, value);
    }

    @Override
    public Long getLongProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getLongInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(Domain domain, String propertyName) {
        String domainValue = getProperty(domain, propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, domainValue);
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return domibusPropertyProviderHelper.filterPropertyNames(predicate);
    }

    @Override
    public List<String> getNestedProperties(Domain domain, String prefix) {
        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(prefix);
        return domibusNestedPropertiesManager.getNestedProperties(domain, propertyMetadata);
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return getNestedProperties(null, prefix);
    }

    @Override
    public boolean containsDomainPropertyKey(Domain domain, String propertyName) {
        final String domainPropertyName = domibusPropertyProviderHelper.getPropertyKeyForDomain(domain, propertyName);
        boolean domainPropertyKeyFound = environment.containsProperty(domainPropertyName);
        if (!domainPropertyKeyFound) {
            domainPropertyKeyFound = environment.containsProperty(propertyName);
        }
        return domainPropertyKeyFound;
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return environment.containsProperty(propertyName);
    }

    @Override
    public void setProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException {
        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
    }

    @Override
    public void setProperty(Domain domain, String propertyName, String propertyValue) throws DomibusPropertyException {
        setProperty(domain, propertyName, propertyValue, true);
    }

}
