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
        // determine if the property is stored in core or in the module manager
        if (propMeta.isStoredGlobally()) {
            // just return it
            return getInternalPropertyValue(domain, propertyName);
        }

        //determine if it is an external or internal property
        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            // just return it
            return getInternalPropertyValue(domain, propertyName);
        }

        //external property then, so call manager for the value
        return getExternalPropertyValue(propertyName, domain, manager);
    }

    public void setInternalOrExternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        // determine if the property is stored in core or in the module manager
        if (propMeta.isStoredGlobally()) {
            setInternalPropertyValue(domain, propertyName, propertyValue, broadcast);
            return;
        }

        //determine if it is an external or internal property
        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            setInternalPropertyValue(domain, propertyName, propertyValue, broadcast);
            return;
        }

        //external property then, so call manager to set the value
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
        } else {
            LOG.debug("Setting property [{}] of manager [{}] on domain [{}].", propertyName, manager, domain);
            manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
        }
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
            LOG.debug("Getting internal property [{}] without domain.", propertyName, domain);
            return domibusPropertyProvider.getInternalProperty(propertyName);
        }
        LOG.debug("Getting internal property [{}] on domain [{}].", propertyName, domain);
        return domibusPropertyProvider.getInternalProperty(domain, propertyName);
    }

    protected String getExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String propertyName) {
        String value;
        if (classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class})) {
            LOG.debug("Calling getKnownPropertyValue method");
            value = propertyManager.getKnownPropertyValue(propertyName);
        } else {
            LOG.debug("Calling deprecated getKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            value = propertyManager.getKnownPropertyValue(currentDomain.getCode(), propertyName);
        }
        return value;
    }

    protected void setExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String name, String value) {
        if (classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class})) {
            LOG.debug("Calling setKnownPropertyValue method");
            propertyManager.setKnownPropertyValue(name, value);
        } else {
            LOG.debug("Calling deprecated setKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            propertyManager.setKnownPropertyValue(currentDomain.getCode(), name, value);
        }
    }
}
