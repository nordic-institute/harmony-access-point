package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Helper class involved in dispatching the calls of the domibus property provider to the core or external property managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyProviderDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderDispatcher.class);

    @Autowired
    ClassUtil classUtil;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    @Autowired
    DomibusPropertyChangeManager domibusPropertyChangeManager;

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
        return manager.getKnownPropertyValue(propertyName);
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
            domain = domainContextProvider.getCurrentDomainSafely();
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
            LOG.trace("Calling getKnownPropertyValue method");
            return propertyManager.getKnownPropertyValue(propertyName);
        }
        LOG.trace("Calling deprecated getKnownPropertyValue method");
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        return propertyManager.getKnownPropertyValue(currentDomain.getCode(), propertyName);
    }

    protected void setExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String name, String value) {
        if (classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class})) {
            LOG.debug("Calling setKnownPropertyValue method");
            propertyManager.setKnownPropertyValue(name, value);
            return;
        }
        LOG.debug("Calling deprecated setKnownPropertyValue method");
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        propertyManager.setKnownPropertyValue(currentDomain.getCode(), name, value);
    }
}
