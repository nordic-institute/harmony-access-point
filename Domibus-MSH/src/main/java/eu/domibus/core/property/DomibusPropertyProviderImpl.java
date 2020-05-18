package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.*;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu, Ion Perpegel
 * @since 4.0
 */
@Service("domibusPropertyProvider")
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    private Set<String> requestedProperties = new HashSet<>();

    @Autowired
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

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
    @Lazy
    GlobalPropertyMetadataManagerImpl globalPropertyMetadataManager;

    @Override
    public String getProperty(String propertyName) {
        return getInternalOrExternalProperty(propertyName, null);
    }

    @Override
    public String getProperty(Domain domain, String propertyName) {
        if (domain == null) {
            throw new DomibusPropertyException("Property " + propertyName + " cannot be retrieved without a domain");
        }
        return getInternalOrExternalProperty(propertyName, domain);
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getIntegerInternal(propertyName, value);
    }

    @Override
    public Long getLongProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getLongInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        String value = getProperty(propertyName);
        return getBooleanInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(Domain domain, String propertyName) {
        String domainValue = getProperty(domain, propertyName);
        return getBooleanInternal(propertyName, domainValue);
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
        setInternalOrExternalProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException {
        setInternalOrExternalProperty(null, propertyName, propertyValue, false);
    }

    //internal methods
    private String getInternalOrExternalProperty(String propertyName, Domain domain) {
        //determine if it is an external or internal property
        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            // it is an internal property
            if (domain == null) {
                return getInternalProperty(propertyName);
            } else {
                return getInternalProperty(domain, propertyName);
            }
        } else {
            //external module property so...
            //if it was already requested -> get local value (as we saved it locally too)
            if (isPropertySavedLocally(propertyName)) {
                return getInternalProperty(propertyName);
            } else {
                //mark requested so that it will be provided internally next time it is requested
                markPropertyAsSavedLocally(propertyName);
                //call manager for the value
                String propertyValue;
                if (domain == null) {
                    propertyValue = manager.getKnownPropertyValue(propertyName);
                } else { //???
                    propertyValue = manager.getKnownPropertyValue(domain.getCode(), propertyName);
                }
                //save the value locally/sync
                if (propertyValue != null) {
                    if (domain == null) {
                        domain = domainContextProvider.getCurrentDomainSafely();
                    }
                    doSetPropertyValue(domain, propertyName, propertyValue);
                }
                //return it
                return propertyValue;
            }
        }
    }

    private void markPropertyAsSavedLocally(String propertyName) {
        requestedProperties.add(propertyName);
    }

    private boolean isPropertySavedLocally(String propertyName) {
        return requestedProperties.contains(propertyName);
    }

    private String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //prop is only global so the current domain doesn't matter
        if (prop.isOnlyGlobal()) {
            LOGGER.trace("Property [{}] is only global (so the current domain doesn't matter) thus retrieving the global value", propertyName);
            return getGlobalProperty(prop);
        }

        //single-tenancy mode
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOGGER.trace("Single tenancy mode: thus retrieving the global value for property [{}]", propertyName);
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2 ( but not 3)
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        //we have a domain in context so try a domain property
        if (currentDomain != null) {
            if (prop.isDomain()) {
                LOGGER.trace("In multi-tenancy mode, property [{}] has domain usage, thus retrieving the domain value.", propertyName);
                return getDomainOrDefaultValue(prop, currentDomain);
            }
            LOGGER.error("Property [{}] is not applicable for a specific domain so null was returned.", propertyName);
            return null;
        } else {
            //current domain being null, it is super or global property (but not both)
            if (prop.isGlobal()) {
                LOGGER.trace("In multi-tenancy mode, property [{}] has global usage, thus retrieving the global value.", propertyName);
                return getGlobalProperty(prop);
            }
            if (prop.isSuper()) {
                LOGGER.trace("In multi-tenancy mode, property [{}] has super usage, thus retrieving the super value.", propertyName);
                return getSuperOrDefaultValue(prop);
            }
            LOGGER.error("Property [{}] is not applicable for super users so null was returned.", propertyName);
            return null;
        }
    }

    private String getInternalProperty(Domain domain, String propertyName) {
        LOGGER.trace("Retrieving value for property [{}] on domain [{}].", propertyName, domain);
//        if (domain == null) {
//            throw new DomibusPropertyException("Property " + propertyName + " cannot be retrieved without a domain");
//        }

        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        //single-tenancy mode
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOGGER.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefaultValue(prop, domain);
    }

    private void setInternalOrExternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        //get current value
        String currentValue;
        if (domain == null) {
            currentValue = getInternalProperty(propertyName);
        } else {
            currentValue = getInternalProperty(domain, propertyName);
        }
        //if they are equal, nothing to do
        if (StringUtils.equals(currentValue, propertyValue)) {
            return;
        }
        //if not:
        // save the new value locally also, no matter if it is an internal or external property
        if (domain == null) {
            domain = domainContextProvider.getCurrentDomainSafely();
            setInternalPropertyValue(domain, propertyName, propertyValue, false);
        } else {
            setInternalPropertyValue(domain, propertyName, propertyValue, broadcast);
        }

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        //if it is an external property, call setProperty on the manager now
        if (manager != null) {
            if (domain == null) {
                manager.setKnownPropertyValue(propertyName, propertyValue);
            } else {
                manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
            }
        }
    }

    protected void setInternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        //code moved from DomibusPropertyManager
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        // validate the property value against the type
        validatePropertyValue(propMeta, propertyValue);

        //kep old value in case of an exception
        String oldValue = getProperty(domain, propertyName);

        //try to set the new value
        doSetPropertyValue(domain, propertyName, propertyValue);

        //let the custom property listeners to do their job
        signalPropertyValueChanged(domain, propertyName, propertyValue, broadcast, propMeta, oldValue);
    }

    protected void validatePropertyValue(DomibusPropertyMetadata propMeta, String propertyValue) throws DomibusPropertyException {
        if (propMeta == null) {
            LOGGER.warn("Property metadata is null; exiting validation.");
            return;
        }

        try {
            DomibusPropertyMetadata.Type type = DomibusPropertyMetadata.Type.valueOf(propMeta.getType());
            DomibusPropertyValidator validator = type.getValidator();
            if (validator == null) {
                LOGGER.debug("Validator for type [{}] of property [{}] is null; exiting validation.", propMeta.getType(), propMeta.getName());
                return;
            }

            if (!validator.isValid(propertyValue)) {
                throw new DomibusPropertyException("Property value [" + propertyValue + "] of property [" + propMeta.getName() + "] does not match property type [" + type.name() + "].");
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Property type [{}] of property [{}] is not known; exiting validation.", propMeta.getType(), propMeta.getName());
        }
    }

    private void doSetPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey;
        //calculate property key
        if (domibusConfigurationService.isMultiTenantAware()) {
            // in multi-tenancy mode - some properties will be prefixed (depends on usage)
            propertyKey = calculatePropertyKeyInMultiTenancy(domain, propertyName);
        } else {
            // in single-tenancy mode - the property key is always the property name
            propertyKey = propertyName;
        }

        //set the value
        setValueInDomibusPropertySource(propertyKey, propertyValue);
    }

    private void signalPropertyValueChanged(Domain domain, String propertyName, String propertyValue, boolean broadcast, DomibusPropertyMetadata propMeta, String oldValue) {
        String domainCode = domain != null ? domain.getCode() : null;
        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        try {
            propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
        } catch (DomibusPropertyException ex) {
            LOGGER.error("An error occurred when executing property change listeners for property [{}]. Reverting to the former value.", propertyName, ex);
            try {
                // revert to old value
                doSetPropertyValue(domain, propertyName, oldValue);
                propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, oldValue, shouldBroadcast);
                // propagate the exception to the client
                throw ex;
            } catch (DomibusPropertyException ex2) {
                LOGGER.error("An error occurred trying to revert property [{}]. Exiting.", propertyName, ex2);
                // failed to revert!!! just report the error
                throw ex2;
            }
        }
    }

    protected Set<String> filterPropertySource(Predicate<String> predicate, PropertySource propertySource) {
        Set<String> filteredPropertyNames = new HashSet<>();
        if (!(propertySource instanceof EnumerablePropertySource)) {
            LOGGER.trace("PropertySource [{}] has been skipped", propertySource.getName());
            return filteredPropertyNames;
        }
        LOGGER.trace("Filtering properties from propertySource [{}]", propertySource.getName());

        EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
        for (String propertyName : enumerablePropertySource.getPropertyNames()) {
            if (predicate.test(propertyName)) {
                LOGGER.trace("Predicate matched property [{}]", propertyName);
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

    protected String calculatePropertyKeyInMultiTenancy(Domain domain, String propertyName) {
        String propertyKey = null;
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (domain != null) {
            propertyKey = calculatePropertyKeyForDomain(domain, propertyName, prop);
        } else {
            propertyKey = calculatePropertyKeyWithoutDomain(propertyName, prop);
        }
        return propertyKey;
    }

    private String calculatePropertyKeyWithoutDomain(String propertyName, DomibusPropertyMetadata prop) {
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

    private String calculatePropertyKeyForDomain(Domain domain, String propertyName, DomibusPropertyMetadata prop) {
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
            LOGGER.debug("Decrypting property [{}]", propertyName);
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
            LOGGER.trace("Returned specific value for property [{}] on domain [{}].", prop.getName(), domain);
            return propValue;
        }
        // didn't find a domain-specific value, try to fallback if acceptable
        if (prop.isWithFallback()) {    //fall-back to the default value from global properties file
            propValue = getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            if (propValue != null) { // found a value->return it
                LOGGER.trace("Returned fallback value for property [{}] on domain [{}].", prop.getName(), domain);
                return propValue;
            }
        }
        LOGGER.debug("Could not find a value for property [{}] on domain [{}].", prop.getName(), domain);
        return null;
    }

    protected String getPropertyKeyForSuper(String propertyName) {
        return "super." + propertyName;
    }

    protected String getPropertyKeyForDomain(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
    }

    private Integer getIntegerInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Integer.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to an integer value", e);
                return getDefaultIntegerValue(propertyName);
            }
        }
        return getDefaultIntegerValue(propertyName);
    }

    protected Long getLongInternal(String propertyName, String customValue) {
        if (customValue != null) {
            try {
                return Long.valueOf(customValue);
            } catch (final NumberFormatException e) {
                LOGGER.warn("Could not parse the property [" + propertyName + "] custom value [" + customValue + "] to a Long value", e);
                return getDefaultLongValue(propertyName);
            }
        }
        return getDefaultLongValue(propertyName);
    }

    protected Integer getDefaultIntegerValue(String propertyName) {
        Integer defaultValue = MapUtils.getInteger(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    protected Long getDefaultLongValue(String propertyName) {
        Long defaultValue = MapUtils.getLong(domibusDefaultProperties, propertyName);
        return checkDefaultValue(propertyName, defaultValue);
    }

    private Boolean getBooleanInternal(String propertyName, String customValue) {
        if (customValue != null) {
            Boolean customBoolean = BooleanUtils.toBooleanObject(customValue);
            if (customBoolean != null) {
                return customBoolean;
            }
            LOGGER.warn("Could not parse the property [{}] custom value [{}] to a boolean value", propertyName, customValue);
            return getDefaultBooleanValue(propertyName);
        }
        return getDefaultBooleanValue(propertyName);
    }

    private Boolean getDefaultBooleanValue(String propertyName) {
        // We need to fetch the Boolean value in two steps as the MapUtils#getBoolean(Properties, String) does not return "null" when the value is an invalid Boolean.
        String defaultValue = MapUtils.getString(domibusDefaultProperties, propertyName);
        Boolean defaultBooleanValue = BooleanUtils.toBooleanObject(defaultValue);
        return checkDefaultValue(propertyName, defaultBooleanValue);
    }

    private <T> T checkDefaultValue(String propertyName, T defaultValue) {
        if (defaultValue == null) {
            throw new IllegalStateException("The default property [" + propertyName + "] is required but was either not found inside the default properties or found having an invalid value");
        }
        LOGGER.debug("Found the property [{}] default value [{}]", propertyName, defaultValue);
        return defaultValue;
    }

}
