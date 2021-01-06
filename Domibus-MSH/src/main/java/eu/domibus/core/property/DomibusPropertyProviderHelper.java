package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DATABASE_GENERAL_SCHEMA;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DomibusPropertyProviderHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderHelper.class);

    private volatile Boolean isMultiTenantAware = null;
    private Object isMultiTenantAwareLock = new Object();

    @Autowired
    protected ConfigurableEnvironment environment;

    @Value("${domibus.database.general.schema}")
    private String generalSchema;

    protected String getPropertyKeyForSuper(String propertyName) {
        return "super." + propertyName;
    }

    protected String getPropertyKeyForDomain(Domain domain, String propertyName) {
        return domain.getCode() + "." + propertyName;
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
    
    // duplicated part of the code from context provider so that we can brake the circular dependency
    protected String getCurrentDomainCode() {
        if (!isMultiTenantAware()) {
            LOG.debug("No multi-tenancy aware: returning the default domain");
            return DomainService.DEFAULT_DOMAIN.getCode();
        }

        String domainCode = LOG.getMDC(DomibusLogger.MDC_DOMAIN);
        LOG.debug("Multi-tenancy aware: returning the domain [{}]", domainCode);

        return domainCode;
    }

    protected Domain getCurrentDomain() {
        String currentDomainCode = getCurrentDomainCode();
        //the domain is created like this in order to avoid the dependency on DomainService ( which creates a cycle)
        // we do not care for the domain name at all in property management, just the domain code
        return new Domain(currentDomainCode, currentDomainCode);
    }

    // duplicated part of the code from context provider so that we can brake the circular dependency
    protected boolean isMultiTenantAware() {
        if (isMultiTenantAware == null) {
            synchronized (isMultiTenantAwareLock) {
                if (isMultiTenantAware == null) {
                    isMultiTenantAware = StringUtils.isNotBlank(generalSchema);
                }
            }
        }
        return isMultiTenantAware;
    }
}
