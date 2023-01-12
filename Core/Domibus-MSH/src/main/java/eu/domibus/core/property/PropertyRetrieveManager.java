package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

/**
 * Responsible for retrieving the values for the internal properties
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class PropertyRetrieveManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyRetrieveManager.class);

    private final ConfigurableEnvironment environment;

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final PropertyProviderHelper propertyProviderHelper;

    public PropertyRetrieveManager(ConfigurableEnvironment environment, GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                   PropertyProviderHelper propertyProviderHelper) {
        this.environment = environment;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderHelper = propertyProviderHelper;
    }

    public String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //prop is only global so the current domain doesn't matter
        if (prop.isOnlyGlobal()) {
            LOG.trace("Property [{}] is only global (so the current domain doesn't matter) thus retrieving the global value", propertyName);
            return getGlobalProperty(prop);
        }

        //single-tenancy mode
        // using internal method to avoid cyclic dependency
        if (!propertyProviderHelper.isMultiTenantAware()) {
            LOG.trace("Single tenancy mode: thus retrieving the global value for property [{}]", propertyName);
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2
        // we do not use domainContextProvider.getCurrentDomain() to avoid cyclic dependency
        Domain currentDomain = propertyProviderHelper.getCurrentDomain();
        //we have a domain in context so try a domain property
        if (currentDomain != null) {
            if (prop.isDomain()) {
                LOG.trace("In multi-tenancy mode, property [{}] has domain usage, thus retrieving the domain value.", propertyName);
                return getDomainOrDefaultValue(prop, currentDomain);
            }
            throw new DomibusPropertyException(
                    String.format("Property [%s] is not applicable for a specific domain (it is global or super), so null was returned for domain [%s].", propertyName, currentDomain));
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
        throw new DomibusPropertyException(
                String.format("Property [%s] is not applicable for global or super usage, so null was returned (as current domain is null).", propertyName));
    }

    public String getInternalProperty(Domain domain, String propertyName) {
        LOG.trace("Retrieving value for property [{}] on domain [{}].", propertyName, domain);

        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        //single-tenancy mode
        // using internal method to avoid cyclic dependency
        if (!propertyProviderHelper.isMultiTenantAware()) {
            LOG.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefaultValue(prop, domain);
    }

    protected String getPropertyValue(String propertyName) {
        return environment.getProperty(propertyName);
    }

    protected String getGlobalProperty(DomibusPropertyMetadata prop) {
        return getPropertyValue(prop.getName());
    }

    protected String getDomainOrDefaultValue(DomibusPropertyMetadata prop, Domain domain) {
        String propertyKey = propertyProviderHelper.getPropertyKeyForDomain(domain, prop.getName());
        return getPropValueOrDefault(propertyKey, prop, domain);
    }

    protected String getSuperOrDefaultValue(DomibusPropertyMetadata prop) {
        String propertyKey = propertyProviderHelper.getPropertyKeyForSuper(prop.getName());
        return getPropValueOrDefault(propertyKey, prop, null);
    }

    protected String getPropValueOrDefault(String propertyKey, DomibusPropertyMetadata prop, Domain domain) {
        String propValue = getPropertyValue(propertyKey);
        if (propValue != null) {
            // found a value->return it
            LOG.trace("Returned specific value for property [{}] on domain [{}].", prop.getName(), domain);
            return propValue;
        }
        // didn't find a domain-specific value, try to fallback if acceptable
        if (prop.isWithFallback()) {
            // fall-back to the default value from global properties file
            propValue = getPropertyValue(prop.getName());
            if (propValue != null) {
                // found a value->return it
                LOG.trace("Returned fallback value for property [{}] on domain [{}].", prop.getName(), domain);
                return propValue;
            }
        }
        LOG.debug("Could not find a value for property [{}] on domain [{}].", prop.getName(), domain);
        return null;
    }
}
