package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.*;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Lazy
    @Autowired
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Autowired
    DomibusPropertyMetadataManager domibusPropertyMetadataManager;

    @Autowired
    DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Autowired
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Override
    public String getProperty(String propertyName) {
        return domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
    }

    @Override
    public String getProperty(Domain domain, String propertyName) {
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
        setProperty(domain, propertyName, propertyValue);
    }

    protected String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = domibusPropertyMetadataManager.getPropertyMetadata(propertyName);

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
        } else {
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
    }

    protected String getInternalProperty(Domain domain, String propertyName) {
        LOG.debug("Retrieving value for property [{}] on domain [{}].", propertyName, domain);

        DomibusPropertyMetadata prop = domibusPropertyMetadataManager.getPropertyMetadata(propertyName);
        //single-tenancy mode
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefaultValue(prop, domain);
    }

    protected void setInternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        DomibusPropertyMetadata propMeta = domibusPropertyMetadataManager.getPropertyMetadata(propertyName);

        // validate the property value against the type
        validatePropertyValue(propMeta, propertyValue);

        //keep old value in case of an exception
        String oldValue = getInternalProperty(domain, propertyName);

        //try to set the new value
        doSetPropertyValue(domain, propertyName, propertyValue);

        //let the custom property listeners to do their job
        signalPropertyValueChanged(domain, propertyName, propertyValue, broadcast, propMeta, oldValue);
    }

    protected void validatePropertyValue(DomibusPropertyMetadata propMeta, String propertyValue) throws DomibusPropertyException {
        if (propMeta == null) {
            LOG.warn("Property metadata is null; exiting validation.");
            return;
        }

        try {
            DomibusPropertyMetadata.Type type = DomibusPropertyMetadata.Type.valueOf(propMeta.getType());
            DomibusPropertyValidator validator = type.getValidator();
            if (validator == null) {
                LOG.debug("Validator for type [{}] of property [{}] is null; exiting validation.", propMeta.getType(), propMeta.getName());
                return;
            }

            if (!validator.isValid(propertyValue)) {
                throw new DomibusPropertyException("Property value [" + propertyValue + "] of property [" + propMeta.getName() + "] does not match property type [" + type.name() + "].");
            }
        } catch (IllegalArgumentException ex) {
            LOG.warn("Property type [{}] of property [{}] is not known; exiting validation.", propMeta.getType(), propMeta.getName());
        }
    }

    protected void doSetPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey;
        //calculate property key
        if (domibusConfigurationService.isMultiTenantAware()) {
            // in multi-tenancy mode - some properties will be prefixed (depends on usage)
            propertyKey = computePropertyKeyInMultiTenancy(domain, propertyName);
        } else {
            // in single-tenancy mode - the property key is always the property name
            propertyKey = propertyName;
        }

        //set the value
        setValueInDomibusPropertySource(propertyKey, propertyValue);
    }

    protected void signalPropertyValueChanged(Domain domain, String propertyName, String propertyValue, boolean broadcast, DomibusPropertyMetadata propMeta, String oldValue) {
        String domainCode = domain != null ? domain.getCode() : null;
        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        LOG.debug("Property [{}] changed its value on domain [{}], broadcasting is [{}]", propMeta, domainCode, shouldBroadcast);

        try {
            propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
        } catch (DomibusPropertyException ex) {
            LOG.error("An error occurred when executing property change listeners for property [{}]. Reverting to the former value.", propertyName, ex);
            try {
                // revert to old value
                doSetPropertyValue(domain, propertyName, oldValue);
                propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, oldValue, shouldBroadcast);
                throw ex;
            } catch (DomibusPropertyException ex2) {
                LOG.error("An error occurred trying to revert property [{}]. Exiting.", propertyName, ex2);
                throw ex2;
            }
        }
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

    protected String computePropertyKeyInMultiTenancy(Domain domain, String propertyName) {
        String propertyKey = null;
        DomibusPropertyMetadata prop = domibusPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (domain != null) {
            propertyKey = computePropertyKeyForDomain(domain, propertyName, prop);
        } else {
            propertyKey = computePropertyKeyWithoutDomain(propertyName, prop);
        }
        return propertyKey;
    }

    private String computePropertyKeyWithoutDomain(String propertyName, DomibusPropertyMetadata prop) {
        String propertyKey = propertyName;
        if (prop.isSuper()) {
            propertyKey = getPropertyKeyForSuper(propertyName);
        } else {
            if (!prop.isGlobal()) {
                String error = String.format("Property [{}] is not applicable for global usage so it cannot be set.", propertyName);
                throw new DomibusPropertyException(error);
            }
        }
        return propertyKey;
    }

    private String computePropertyKeyForDomain(Domain domain, String propertyName, DomibusPropertyMetadata prop) {
        String propertyKey;
        if (prop.isDomain()) {
            propertyKey = getPropertyKeyForDomain(domain, propertyName);
        } else {
            String error = String.format("Property [{}] is not applicable for a specific domain so it cannot be set.", propertyName);
            throw new DomibusPropertyException(error);
        }
        return propertyKey;
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
        if (propValue != null) { // found a value->return it
            LOG.trace("Returned specific value for property [{}] on domain [{}].", prop.getName(), domain);
            return propValue;
        }
        // didn't find a domain-specific value, try to fallback if acceptable
        if (prop.isWithFallback()) {    // fall-back to the default value from global properties file
            propValue = getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            if (propValue != null) { // found a value->return it
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
