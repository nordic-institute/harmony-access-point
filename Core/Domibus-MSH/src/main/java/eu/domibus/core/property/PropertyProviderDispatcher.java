package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Helper class involved in dispatching the calls of the domibus property provider to the core or external property managers
 * Responsible also for caching the values
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PropertyProviderDispatcher {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyProviderDispatcher.class);

    private static final String CACHE_KEY_EXPRESSION = "#root.target.getCacheKeyValue(#domain, #propertyName)";

    private final ClassUtil classUtil;

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final PropertyRetrieveManager propertyRetrieveManager;

    private final PropertyChangeManager propertyChangeManager;

    private final PropertyProviderHelper propertyProviderHelper;

    public PropertyProviderDispatcher(GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                      PropertyRetrieveManager propertyRetrieveManager,
                                      PropertyChangeManager propertyChangeManager, ClassUtil classUtil,
                                      PropertyProviderHelper propertyProviderHelper) {
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyRetrieveManager = propertyRetrieveManager;
        this.propertyChangeManager = propertyChangeManager;
        this.classUtil = classUtil;
        this.propertyProviderHelper = propertyProviderHelper;
    }

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
            manager.setKnownPropertyValue(propertyName, propertyValue);
            return;
        }
        LOG.debug("Setting property [{}] of manager [{}] on domain [{}].", propertyName, manager, domain);
        manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
    }

    protected void setInternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        if (domain == null) {
            LOG.debug("Setting internal property [{}] with value [{}] without domain.", propertyName, propertyValue);
            domain = propertyProviderHelper.getCurrentDomain();
        }
        LOG.debug("Setting internal property [{}] on domain [{}] with value [{}].", propertyName, domain, propertyValue);
        propertyChangeManager.setPropertyValue(domain, propertyName, propertyValue, broadcast);
    }

    protected String getInternalPropertyValue(Domain domain, String propertyName) {
        if (domain == null) {
            LOG.trace("Getting internal property [{}] without domain.", propertyName);
            return propertyRetrieveManager.getInternalProperty(propertyName);
        }
        LOG.trace("Getting internal property [{}] on domain [{}].", propertyName, domain);
        return propertyRetrieveManager.getInternalProperty(domain, propertyName);
    }

    protected String getExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String propertyName) {
        if (classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class})) {
            LOG.trace("Calling getKnownPropertyValue(propertyName) method");
            return propertyManager.getKnownPropertyValue(propertyName);
        }
        String currentDomainCode = propertyProviderHelper.getCurrentDomainCode();
        LOG.trace("Going to call getKnownPropertyValue for current domain [{}] as property manager [{}] doesn't have the method without domain defined", currentDomainCode, propertyManager);
        return propertyManager.getKnownPropertyValue(currentDomainCode, propertyName);
    }

    //this method needs to be public for the ehCache to be able to call it
    public String getCacheKeyValue(Domain domain, String propertyName) {
        return propertyProviderHelper.getCacheKeyValue(domain, globalPropertyMetadataManager.getPropertyMetadata(propertyName));
    }
}
