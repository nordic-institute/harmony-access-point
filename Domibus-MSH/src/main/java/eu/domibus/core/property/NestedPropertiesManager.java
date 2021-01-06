package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class NestedPropertiesManager {

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    DomibusPropertyProviderHelper domibusPropertyProviderHelper;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NestedPropertiesManager.class);

    protected List<String> getNestedProperties(Domain domain, String prefix) {
        String propertyPrefix = getPropertyPrefix(domain, prefix);
        LOG.debug("Getting nested properties for prefix [{}]", propertyPrefix);

        List<String> result = new ArrayList<>();
        Set<String> propertiesStartingWithPrefix = filterPropertyNames(property -> StringUtils.startsWith(property, propertyPrefix));
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

    protected Set<String> filterPropertyNames(Predicate<String> predicate) {
        Set<String> result = new HashSet<>();
        for (PropertySource propertySource : environment.getPropertySources()) {
            Set<String> propertySourceNames = filterPropertySource(predicate, propertySource);
            result.addAll(propertySourceNames);
        }
        return result;
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

        if (domibusPropertyProviderHelper.isMultiTenantAware()) {
            Domain currentDomain = domain;

            if (currentDomain == null) {
                // we do not use domainContextProvider.getCurrentDomain() to avoid cyclic dependency
                currentDomain = domibusPropertyProviderHelper.getCurrentDomain();
                LOG.trace("Using current domain [{}]", currentDomain);
            }

            LOG.trace("Multi tenancy mode: getting prefix taking into account domain [{}]", domain);
            propertyPrefix = computePropertyPrefix(currentDomain, prefix);
        }
        propertyPrefix = propertyPrefix + ".";
        return propertyPrefix;
    }
}
