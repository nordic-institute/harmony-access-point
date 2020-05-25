package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class involved in dispatching the calls of the domibus property provider to the core or external property managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyProviderDispatcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderDispatcher.class);

    private Set<String> requestedProperties = new HashSet<>();

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

    protected String getInternalOrExternalProperty(String propertyName, Domain domain) throws DomibusPropertyException {
        //determine if it is an external or internal property
        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            // it is an internal property
            return getInternalPropertyValue(domain, propertyName);
        }
        //if it was already requested -> get local value (as we saved it locally too)
        if (isPropertySavedLocally(propertyName)) {
            return getInternalPropertyValue(domain, propertyName);
        }
        //mark as available locally so that it will be provided internally next time it is requested
        markPropertyAsSavedLocally(propertyName);
        //call manager for the value
        String propertyValue = getExternalPropertyValue(propertyName, domain, manager);
        //save the value locally/sync
        if (propertyValue != null) {
            DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            if (prop.isDomain() && domain == null) {
                domain = domainContextProvider.getCurrentDomainSafely();
            }
            domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);
        }
        return propertyValue;
    }

    protected void setInternalOrExternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        //get current value
        String currentValue = getInternalPropertyValue(domain, propertyName);
        //if they are equal, nothing to do
        if (StringUtils.equals(currentValue, propertyValue)) {
            return;
        }
        // save the new value locally also, no matter if it is an internal or external property
        setInternalPropertyValue(domain, propertyName, propertyValue, broadcast);

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        //if it is an external property, call setProperty on the manager too
        if (manager != null) {
            setExternalPropertyValue(domain, propertyName, propertyValue, broadcast, manager);
        }
    }

    protected String getExternalPropertyValue(String propertyName, Domain domain, DomibusPropertyManagerExt manager) {
        if (domain == null) {
            return getExternalModulePropertyValue(manager, propertyName);
        }
        return manager.getKnownPropertyValue(domain.getCode(), propertyName);
    }

    protected void setExternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast, DomibusPropertyManagerExt manager) {
        if (domain == null) {
            setExternalModulePropertyValue(manager, propertyName, propertyValue);
        } else {
            manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
        }
    }

    protected void setInternalPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        if (domain == null) {
            domain = domainContextProvider.getCurrentDomainSafely();
        }
        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propertyValue, broadcast);
    }

    protected String getInternalPropertyValue(Domain domain, String propertyName) {
        if (domain == null) {
            return domibusPropertyProvider.getInternalProperty(propertyName);
        }
        return domibusPropertyProvider.getInternalProperty(domain, propertyName);
    }

    protected void markPropertyAsSavedLocally(String propertyName) {
        requestedProperties.add(propertyName);
    }

    protected boolean isPropertySavedLocally(String propertyName) {
        return requestedProperties.contains(propertyName);
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
