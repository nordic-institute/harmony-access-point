package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_LENGTH_MAX;

/**
 * Helper class involved in dispatching the calls of the domibus property provider to the core or external property managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyProviderDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderDispatcher.class);

    // it is possible for getCurrentDomainCode() to return null for the first stages of bootstrap process
    // for global properties but it is acceptable since they are not going to mess with super properties
    private static final String CACHE_KEY_EXPRESSION = "(#domain != null ? #domain.getCode() : " +
            "(#root.target.getCurrentDomainCode()) == null ? \"global\" : #root.target.getCurrentDomainCode()) + ':' + #propertyName";

    @Autowired
    ClassUtil classUtil;

    @Autowired
    public DomainContextProvider domainContextProvider;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Autowired
    DomibusPropertyChangeManager domibusPropertyChangeManager;

    @Autowired
    protected DomainService domainService;

    @Cacheable(value = DomibusCacheService.DOMIBUS_PROPERTY_CACHE, key = CACHE_KEY_EXPRESSION)
    public String getInternalOrExternalProperty(String propertyName, Domain domain) throws DomibusPropertyException {

        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (propMeta.isStoredGlobally()) {
            return getInternalPropertyValue(domain, propertyName);
        }

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            throw new DomibusPropertyException("Could not find manager for not globally stored property " + propertyName);
        }

        return getExternalPropertyValue(propertyName, domain, manager);
    }

    @CacheEvict(value = DomibusCacheService.DOMIBUS_PROPERTY_CACHE, key = CACHE_KEY_EXPRESSION, beforeInvocation = true)
    public void setInternalOrExternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        Integer maxLength = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PROPERTY_LENGTH_MAX);
        if (maxLength > 0 && propertyValue != null && propertyValue.length() > maxLength) {
            throw new IllegalArgumentException("Invalid property value. Maximum accepted length is: " + maxLength);
        }

        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (propMeta.isStoredGlobally()) {
            setInternalPropertyValue(domain, propertyName, propertyValue, broadcast);
            return;
        }

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            throw new DomibusPropertyException("Could not find manager for not globally stored property " + propertyName);
        }

        setExternalPropertyValue(domain, propertyName, propertyValue, broadcast, manager);
    }

    protected String getExternalPropertyValue(String propertyName, Domain domain, DomibusPropertyManagerExt manager) {
        if (domain == null) {
            LOG.debug("Getting property [{}] of manager [{}] without domain.", propertyName, manager);
            return getExternalModulePropertyValue(manager, propertyName);
        }
        LOG.debug("Getting property [{}] on domain [{}] of manager [{}].", propertyName, domain, manager);
        return manager.getKnownPropertyValue(domain.getCode(), propertyName);
    }

    protected void setExternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast, DomibusPropertyManagerExt manager) {
        if (domain == null) {
            LOG.debug("Setting property [{}] of manager [{}] without domain.", propertyName, manager);
            setExternalModulePropertyValue(manager, propertyName, propertyValue);
            return;
        }
        LOG.debug("Setting property [{}] of manager [{}] on domain [{}].", propertyName, manager, domain);
        manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
    }

    protected void setInternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        if (domain == null) {
            LOG.debug("Setting internal property [{}] with value [{}] without domain.", propertyName, propertyValue);
            domain = getCurrentDomain();
        }
        LOG.debug("Setting internal property [{}] on domain [{}] with value [{}].", propertyName, domain, propertyValue);
        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propertyValue, broadcast);
    }

    protected String getInternalPropertyValue(Domain domain, String propertyName) {
        if (domain == null) {
            LOG.trace("Getting internal property [{}] without domain.", propertyName);
            return domibusPropertyProvider.getInternalProperty(propertyName);
        }
        LOG.trace("Getting internal property [{}] on domain [{}].", propertyName, domain);
        return domibusPropertyProvider.getInternalProperty(domain, propertyName);
    }

    protected String getExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String propertyName) {
        if (classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class})) {
            LOG.trace("Calling getKnownPropertyValue(propertyName) method");
            return propertyManager.getKnownPropertyValue(propertyName);
        }
        String currentDomainCode = getCurrentDomainCode();
        LOG.trace("Going to call getKnownPropertyValue for current domain [{}] as property manager [{}] doesn't have the method without domain defined", currentDomainCode, propertyManager);
        return propertyManager.getKnownPropertyValue(currentDomainCode, propertyName);
    }

    protected void setExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String name, String value) {
        if (classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class})) {
            LOG.debug("Calling setKnownPropertyValue method");
            propertyManager.setKnownPropertyValue(name, value);
            return;
        }
        LOG.debug("Calling deprecated setKnownPropertyValue method");
        String currentDomainCode = getCurrentDomainCode();
        propertyManager.setKnownPropertyValue(currentDomainCode, name, value);
    }

    // duplicated part of the code from context provider so that we can brake the circular dependency
    // need to be public to be called from cache expression
    public String getCurrentDomainCode() {
        if (!domibusPropertyProvider.isMultiTenantAware()) {
            LOG.debug("No multi-tenancy aware: returning the default domain");
            return DomainService.DEFAULT_DOMAIN.getCode();
        }

        String domainCode = LOG.getMDC(DomibusLogger.MDC_DOMAIN);
        LOG.debug("Multi-tenancy aware: returning the domain [{}]", domainCode);

        return domainCode;
    }

    protected Domain getCurrentDomain() {
        return domainService.getDomain(getCurrentDomainCode());
    }
}
