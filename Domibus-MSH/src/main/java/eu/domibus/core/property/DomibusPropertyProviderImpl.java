package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    protected DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Autowired
    protected PrimitivePropertyTypesManager primitivePropertyTypesManager;

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
        Set<String> result = new HashSet<>();
        for (PropertySource propertySource : environment.getPropertySources()) {
            Set<String> propertySourceNames = filterPropertySource(predicate, propertySource);
            result.addAll(propertySourceNames);
        }
        return result;
    }

    @Override
    public boolean containsDomainPropertyKey(Domain domain, String propertyName) {
        final String domainPropertyName = getPropertyKeyForDomain(domain, propertyName);
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
        if (domain == null) {
            throw new DomibusPropertyException("Property " + propertyName + " cannot be set without a domain");
        }
        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException {
        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, false);
    }

    @Override
    public void setProperty(Domain domain, String propertyName, String propertyValue) throws DomibusPropertyException {
        setProperty(domain, propertyName, propertyValue, false);
    }

    protected String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //prop is only global so the current domain doesn't matter
        if (prop.isOnlyGlobal()) {
            LOG.trace("Property [{}] is only global (so the current domain doesn't matter) thus retrieving the global value", propertyName);
            return getGlobalProperty(prop);
        }

        //single-tenancy mode
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("Single tenancy mode: thus retrieving the global value for property [{}]", propertyName);
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2 ( but not 3)
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        //we have a domain in context so try a domain property
        if (currentDomain != null) {
            if (prop.isDomain()) {
                LOG.trace("In multi-tenancy mode, property [{}] has domain usage, thus retrieving the domain value.", propertyName);
                return getDomainOrDefaultValue(prop, currentDomain);
            }
            LOG.error("Property [{}] is not applicable for a specific domain so null was returned.", propertyName);
            return null;
        }
        //current domain being null, it is super or global property (but not both)
        if (prop.isGlobal()) {
            LOG.trace("In multi-tenancy mode, property [{}] has global usage, thus retrieving the global value.", propertyName);
            return getGlobalProperty(prop);
        }
        if (prop.isSuper()) {
            LOG.trace("In multi-tenancy mode, property [{}] has super usage, thus retrieving the super value.", propertyName);
            return getSuperOrDefaultValue(prop);
        }
        LOG.error("Property [{}] is not applicable for super users so null was returned.", propertyName);
        return null;

    }

    protected String getInternalProperty(Domain domain, String propertyName) {
        LOG.debug("Retrieving value for property [{}] on domain [{}].", propertyName, domain);

        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (prop.isDomain()) {
            if (domain == null) {
                throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
            }
            return getDomainOrDefaultValue(prop, domain);
        }
        if (prop.isSuper()) {
            return getSuperOrDefaultValue(prop);
        }
        return getGlobalProperty(prop);
    }

    protected Set<String> filterPropertySource(Predicate<String> predicate, PropertySource propertySource) {
        Set<String> filteredPropertyNames = new HashSet<>();
        if (!(propertySource instanceof EnumerablePropertySource)) {
            LOG.trace("PropertySource [{}] has been skipped", propertySource.getName());
            return filteredPropertyNames;
        }
        LOG.trace("Filtering properties from propertySource [{}]", propertySource.getName());

        EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
        for (String propertyName : enumerablePropertySource.getPropertyNames()) {
            if (predicate.test(propertyName)) {
                LOG.trace("Predicate matched property [{}]", propertyName);
                filteredPropertyNames.add(propertyName);
            }
        }
        return filteredPropertyNames;
    }

    protected void setValueInDomibusPropertySource(String propertyKey, String propertyValue) {
        MutablePropertySources propertySources = environment.getPropertySources();
        DomibusPropertiesPropertySource domibusPropertiesPropertySource = (DomibusPropertiesPropertySource) propertySources.get(DomibusPropertiesPropertySource.NAME);
        domibusPropertiesPropertySource.setProperty(propertyKey, propertyValue);
    }

    /**
     * Get the value from the system environment properties;
     * if not found, get the value from the system properties;
     * if not found, get the value from Domibus properties;
     * if still not found, look inside the Domibus default properties.
     *
     * @param propertyName the property name
     * @return The value of the property as found in the system properties, the Domibus properties or inside the default Domibus properties.
     */
    protected String getPropertyValue(String propertyName, Domain domain, boolean decrypt) {
        String result = environment.getProperty(propertyName);

        if (decrypt && passwordEncryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}]", propertyName);
            result = passwordEncryptionService.decryptProperty(domain, propertyName, result);
        }

        return result;
    }

    protected String getGlobalProperty(DomibusPropertyMetadata prop) {
        return getPropertyValue(prop.getName(), null, prop.isEncrypted());
    }

    protected String getDomainOrDefaultValue(DomibusPropertyMetadata prop, Domain domain) {
        String propertyKey = getPropertyKeyForDomain(domain, prop.getName());
        return getPropValueOrDefault(propertyKey, prop, domain);
    }

    protected String getSuperOrDefaultValue(DomibusPropertyMetadata prop) {
        String propertyKey = getPropertyKeyForSuper(prop.getName());
        return getPropValueOrDefault(propertyKey, prop, null);
    }

    protected String getPropValueOrDefault(String propertyKey, DomibusPropertyMetadata prop, Domain domain) {
        String propValue = getPropertyValue(propertyKey, domain, prop.isEncrypted());
        if (propValue != null) {
            // found a value->return it
            LOG.trace("Returned specific value for property [{}] on domain [{}].", prop.getName(), domain);
            return propValue;
        }
        // didn't find a domain-specific value, try to fallback if acceptable
        if (prop.isWithFallback()) {
            // fall-back to the default value from global properties file
            propValue = getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            if (propValue != null) {
                // found a value->return it
                LOG.trace("Returned fallback value for property [{}] on domain [{}].", prop.getName(), domain);
                return propValue;
            }
        }
        LOG.debug("Could not find a value for property [{}] on domain [{}].", prop.getName(), domain);
        return null;
    }

    protected String getPropertyKeyForSuper(String propertyName) {
        return "super." + propertyName;
    }

    protected String getPropertyKeyForDomain(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
    }

}
