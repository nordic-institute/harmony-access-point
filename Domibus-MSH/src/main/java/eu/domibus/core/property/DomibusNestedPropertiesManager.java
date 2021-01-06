package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadata.NAME_SEPARATOR;

/**
 * Responsible with nested properties management
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomibusNestedPropertiesManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusNestedPropertiesManager.class);

    protected ConfigurableEnvironment environment;

    DomibusPropertyProviderHelper domibusPropertyProviderHelper;

    public DomibusNestedPropertiesManager(ConfigurableEnvironment environment, DomibusPropertyProviderHelper domibusPropertyProviderHelper) {
        this.environment = environment;
        this.domibusPropertyProviderHelper = domibusPropertyProviderHelper;
    }

    public List<String> getNestedProperties(DomibusPropertyMetadata propMeta) {
        return getNestedProperties(null, propMeta);
    }

    protected List<String> getNestedProperties(Domain domain, DomibusPropertyMetadata propertyMetadata) {
        String propertyPrefix = getPropertyPrefix(domain, propertyMetadata);
        LOG.debug("Getting nested properties for prefix [{}]", propertyPrefix);

        List<String> result = new ArrayList<>();
        Set<String> propertiesStartingWithPrefix = domibusPropertyProviderHelper.filterPropertyNames(property -> StringUtils.startsWith(property, propertyPrefix));
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

    protected String getPropertyPrefix(Domain domain, DomibusPropertyMetadata propertyMetadata) {
        String propPrefix = computePropertyPrefix(domain, propertyMetadata);
        if (propPrefix != null) {
            return propPrefix + NAME_SEPARATOR;
        }
        return null;
    }

    /**
     * Composes the property name based on the property usage, domain and name itself
     * <p>
     * Eg. Given domain code = digit and property prefix = jmsplugin.queue.reply.routing it will return digit.jmsplugin.queue.reply.routing
     *
     * @param domain           The domain for which the property
     * @param propertyMetadata
     * @return
     */
    protected String computePropertyPrefix(Domain domain, DomibusPropertyMetadata propertyMetadata) {
        String propertyName = propertyMetadata.getName();
        //prop is only global so the current domain doesn't matter
        if (propertyMetadata.isOnlyGlobal()) {
            LOG.trace("Property [{}] is only global (so the current domain doesn't matter) thus returning the original name.", propertyName);
            return propertyName;
        }

        //single-tenancy mode
        if (!domibusPropertyProviderHelper.isMultiTenantAware()) {
            LOG.trace("Single tenancy mode: thus returning the original name for property [{}]", propertyName);
            return propertyName;
        }

        //multi-tenancy mode
        //domain or super property or a combination of 2
        Domain currentDomain = domain != null ? domain : domibusPropertyProviderHelper.getCurrentDomain();
        //we have a domain in context so try a domain property
        if (currentDomain != null) {
            if (propertyMetadata.isDomain()) {
                LOG.trace("In multi-tenancy mode, property [{}] has domain usage, thus returning the property key for domain [{}].", propertyName, currentDomain);
                return domibusPropertyProviderHelper.getPropertyKeyForDomain(currentDomain, propertyName);
            }
            LOG.error("Property [{}] is not applicable for a specific domain so null was returned.", propertyName);
            return null;
        }
        //current domain being null, it is super or global property (but not both)
        if (propertyMetadata.isGlobal()) {
            LOG.trace("In multi-tenancy mode, property [{}] has global usage, thus thus returning the original name.", propertyName);
            return propertyName;
        }
        if (propertyMetadata.isSuper()) {
            LOG.trace("In multi-tenancy mode, property [{}] has super usage, thus returning the property key for super.", propertyName);
            return domibusPropertyProviderHelper.getPropertyKeyForSuper(propertyName);
        }
        LOG.error("Property [{}] is not applicable for super users so null was returned.", propertyName);
        return null;
    }

}
