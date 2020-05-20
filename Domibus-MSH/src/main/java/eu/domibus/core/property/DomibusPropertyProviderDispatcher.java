package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class involved in dispatching the class to get and set domibus property values to the core or external property managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyProviderDispatcher {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyProviderDispatcher.class);

    private Set<String> requestedProperties = new HashSet<>();

    @Autowired
    ClassUtil classUtil;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    @Lazy
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Autowired
    @Lazy
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    protected String getInternalOrExternalProperty(String propertyName, Domain domain) {
        //determine if it is an external or internal property
        DomibusPropertyManagerExt manager = domibusPropertyMetadataManager.getManagerForProperty(propertyName);
        if (manager == null) {
            // it is an internal property
            if (domain == null) {
                return domibusPropertyProvider.getInternalProperty(propertyName);
            } else {
                return domibusPropertyProvider.getInternalProperty(domain, propertyName);
            }
        } else {
            //external module property so...
            //if it was already requested -> get local value (as we saved it locally too)
            if (isPropertySavedLocally(propertyName)) {
                return domibusPropertyProvider.getInternalProperty(propertyName);
            } else {
                //mark requested so that it will be provided internally next time it is requested
                markPropertyAsSavedLocally(propertyName);
                //call manager for the value
                String propertyValue;
                if (domain == null) {
                    propertyValue = getExternalModulePropertyValue(manager, propertyName); // manager.getKnownPropertyValue(propertyName);
                } else {
                    propertyValue = manager.getKnownPropertyValue(domain.getCode(), propertyName);
                }
                //save the value locally/sync
                if (propertyValue != null) {
                    if (domain == null) {
                        domain = domainContextProvider.getCurrentDomainSafely();
                    }
                    domibusPropertyProvider.doSetPropertyValue(domain, propertyName, propertyValue);
                }
                return propertyValue;
            }
        }
    }

    protected void setInternalOrExternalProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) {
        //get current value
        String currentValue;
        if (domain == null) {
            currentValue = domibusPropertyProvider.getInternalProperty(propertyName);
        } else {
            currentValue = domibusPropertyProvider.getInternalProperty(domain, propertyName);
        }
        //if they are equal, nothing to do
        if (StringUtils.equals(currentValue, propertyValue)) {
            return;
        }
        //if not:
        // save the new value locally also, no matter if it is an internal or external property
        if (domain == null) {
            domain = domainContextProvider.getCurrentDomainSafely();
            domibusPropertyProvider.setInternalProperty(domain, propertyName, propertyValue, true);
        } else { //get current domain, compare it with the param and throw in case of difference???
            domibusPropertyProvider.setInternalProperty(domain, propertyName, propertyValue, broadcast);
        }

        DomibusPropertyManagerExt manager = domibusPropertyMetadataManager.getManagerForProperty(propertyName);
        //if it is an external property, call setProperty on the manager now
        if (manager != null) {
            if (domain == null) {
                setExternalModulePropertyValue(manager, propertyName, propertyValue); //manager.setKnownPropertyValue(propertyName, propertyValue);
            } else {
                manager.setKnownPropertyValue(domain.getCode(), propertyName, propertyValue, broadcast);
            }
        }
    }

    private void markPropertyAsSavedLocally(String propertyName) {
        requestedProperties.add(propertyName);
    }

    private boolean isPropertySavedLocally(String propertyName) {
        return requestedProperties.contains(propertyName);
    }

    protected String getExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String propertyName) {
        String value;
        if (classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class})) {
            LOGGER.debug("Calling getKnownPropertyValue method");
            value = propertyManager.getKnownPropertyValue(propertyName);
        } else {
            LOGGER.debug("Calling deprecated getKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            value = propertyManager.getKnownPropertyValue(currentDomain.getCode(), propertyName);
        }
        return value;
    }

    protected void setExternalModulePropertyValue(DomibusPropertyManagerExt propertyManager, String name, String value) {
        if (classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class})) {
            LOGGER.debug("Calling setKnownPropertyValue method");
            propertyManager.setKnownPropertyValue(name, value);
        } else {
            LOGGER.debug("Calling deprecated setKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            propertyManager.setKnownPropertyValue(currentDomain.getCode(), name, value);
        }
    }
}
