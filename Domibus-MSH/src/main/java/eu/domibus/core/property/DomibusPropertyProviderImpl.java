package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_GENERAL_SCHEMA;

/**
 * Single entry point for getting and setting internal and external domibus properties
 *
 * @author Cosmin Baciu, Ion Perpegel
 * @since 4.0
 */
@Service
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    private volatile Boolean isMultiTenantAware = null;
    private Object isMultiTenantAwareLock = new Object();

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected PasswordEncryptionService passwordEncryptionService;

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

    /**
     * Composes the property name based on the domain and queue prefix
     * <p>
     * Eg. Given domain code = digit and queue prefix = jmsplugin.queue.reply.routing it will return digit.jmsplugin.queue.reply.routing
     *
     * @param domain         The domain for which the property
     * @param propertyPrefix
     * @return
     */
    protected String computePropertyPrefix(Domain domain, String propertyPrefix) {
        String result = domain.getCode() + "." + propertyPrefix;
        LOG.debug("Compute queue prefix [{}]", result);
        return result;
    }

    /**
     * Gets the property prefix taking into account the current domain
     *
     * @param prefix The initial property prefix
     * @return The computed property prefix
     */
    protected String getPropertyPrefix(Domain domain, String prefix) {
        String propertyPrefix = prefix;

        if (isMultiTenantAware()) {
            Domain currentDomain = domain;

            if (currentDomain == null) {
                currentDomain = domainContextProvider.getCurrentDomain();
                LOG.trace("Using current domain [{}]", currentDomain);
            }

            LOG.trace("Multi tenancy mode: getting prefix taking into account domain [{}]", domain);
            propertyPrefix = computePropertyPrefix(currentDomain, prefix);
        }
        propertyPrefix = propertyPrefix + ".";
        return propertyPrefix;
    }

    @Override
    public List<String> getNestedProperties(Domain domain, String prefix) {
        String propertyPrefix = getPropertyPrefix(domain, prefix);
        LOG.debug("Getting nested properties for prefix [{}]", propertyPrefix);

        List<String> result = new ArrayList<>();
        Set<String> propertiesStartingWithPrefix = filterPropertiesName(property -> StringUtils.startsWith(property, propertyPrefix));
        if (CollectionUtils.isEmpty(propertiesStartingWithPrefix)) {
            LOG.debug("No properties found starting with prefix [{}]", propertyPrefix);
            return result;
        }
        LOG.debug("Found properties [{}] starting with prefix [{}]", propertiesStartingWithPrefix, propertyPrefix);
        List<String> firstLevelProperties = propertiesStartingWithPrefix.stream()
                .map(property -> StringUtils.substringAfter(property, propertyPrefix))
                .filter(property -> StringUtils.containsNone(property, ".")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(firstLevelProperties)) {
            LOG.debug("No first level properties found starting with prefix [{}]", propertyPrefix);
            return result;
        }
        LOG.debug("Found first level properties [{}] starting with prefix [{}]", firstLevelProperties, propertyPrefix);
        return firstLevelProperties;
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return getNestedProperties(null, prefix);
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

    protected String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //prop is only global so the current domain doesn't matter
        if (prop.isOnlyGlobal()) {
            LOG.trace("Property [{}] is only global (so the current domain doesn't matter) thus retrieving the global value", propertyName);
            return getGlobalProperty(prop);
        }

        //single-tenancy mode
        if (!isMultiTenantAware()) {
            LOG.trace("Single tenancy mode: thus retrieving the global value for property [{}]", propertyName);
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2 
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
        LOG.trace("Retrieving value for property [{}] on domain [{}].", propertyName, domain);

        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        //single-tenancy mode
        if (!isMultiTenantAware()) {
            LOG.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefaultValue(prop, domain);
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

        DomibusPropertiesPropertySource updatedDomibusPropertiesSource = (DomibusPropertiesPropertySource) propertySources.get(DomibusPropertiesPropertySource.UPDATED_PROPERTIES_NAME);
        updatedDomibusPropertiesSource.setProperty(propertyKey, propertyValue);
    }

    /**
     * First try to get the value from the collection of property values updated at runtime;
     * if not found, get the value from the system environment properties;
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

    // duplicated part of the code from context provider so that we can brake the circular dependency
    protected boolean isMultiTenantAware() {
        if (isMultiTenantAware == null) {
            synchronized (isMultiTenantAwareLock) {
                if (isMultiTenantAware == null) {
                    String propValue = getPropertyValue(DOMIBUS_DATABASE_GENERAL_SCHEMA, null, false);
                    isMultiTenantAware = StringUtils.isNotBlank(propValue);
                }
            }
        }
        return isMultiTenantAware;
    }
}
