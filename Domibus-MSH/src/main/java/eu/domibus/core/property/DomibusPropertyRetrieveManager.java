package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

/**
 * Single entry point for getting and setting internal and external domibus properties
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomibusPropertyRetrieveManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyRetrieveManager.class);

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    protected PasswordDecryptionService passwordDecryptionService;

    @Autowired
    DomibusPropertyProviderHelper domibusPropertyProviderHelper;

    protected String getInternalProperty(String propertyName) {
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //prop is only global so the current domain doesn't matter
        if (prop.isOnlyGlobal()) {
            LOG.trace("Property [{}] is only global (so the current domain doesn't matter) thus retrieving the global value", propertyName);
            return getGlobalProperty(prop);
        }

        //single-tenancy mode
        // using internal method to avoid cyclic dependency
        if (!domibusPropertyProviderHelper.isMultiTenantAware()) {
            LOG.trace("Single tenancy mode: thus retrieving the global value for property [{}]", propertyName);
            return getGlobalProperty(prop);
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2
        // we do not use domainContextProvider.getCurrentDomain() to avoid cyclic dependency
        Domain currentDomain = domibusPropertyProviderHelper.getCurrentDomain();
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
        // using internal method to avoid cyclic dependency
        if (!domibusPropertyProviderHelper.isMultiTenantAware()) {
            LOG.trace("In single-tenancy mode, retrieving global value for property [{}] on domain [{}].", propertyName, domain);
            return getGlobalProperty(prop);
        }

        if (!prop.isDomain()) {
            throw new DomibusPropertyException("Property " + propertyName + " is not domain specific so it cannot be retrieved for domain " + domain);
        }

        return getDomainOrDefaultValue(prop, domain);
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
    protected String getPropertyValue(String propertyName, Domain domain, boolean encrypted) {
        String result = environment.getProperty(propertyName);

        if (encrypted && passwordDecryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}]", propertyName);
            result = passwordDecryptionService.decryptProperty(domain, propertyName, result);
        }

        return result;
    }

    protected String getGlobalProperty(DomibusPropertyMetadata prop) {
        return getPropertyValue(prop.getName(), null, prop.isEncrypted());
    }

    protected String getDomainOrDefaultValue(DomibusPropertyMetadata prop, Domain domain) {
        String propertyKey = domibusPropertyProviderHelper.getPropertyKeyForDomain(domain, prop.getName());
        return getPropValueOrDefault(propertyKey, prop, domain);
    }

    protected String getSuperOrDefaultValue(DomibusPropertyMetadata prop) {
        String propertyKey = domibusPropertyProviderHelper.getPropertyKeyForSuper(prop.getName());
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
}
