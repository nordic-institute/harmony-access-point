package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Helper class involved in changing of domibus properties at runtime
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyChangeManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyChangeManager.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Autowired
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Lazy
    @Autowired
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    protected void setPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        // validate the property value against the type
        validatePropertyValue(propMeta, propertyValue);

        //keep old value in case of an exception
        String oldValue = getInternalPropertyValue(domain, propertyName);

        //try to set the new value
        doSetPropertyValue(domain, propertyName, propertyValue);

        //let the custom property listeners to do their job
        signalPropertyValueChanged(domain, propertyName, propertyValue, broadcast, propMeta, oldValue);
    }

    private String getInternalPropertyValue(Domain domain, String propertyName) {
        if (domain == null) {
            return domibusPropertyProvider.getInternalProperty(propertyName);
        }
        return domibusPropertyProvider.getInternalProperty(domain, propertyName);
    }

    protected void validatePropertyValue(DomibusPropertyMetadata propMeta, String propertyValue) throws DomibusPropertyException {
        if (propMeta == null) {
            LOG.warn("Property metadata is null; exiting validation.");
            return;
        }

        try {
            DomibusPropertyMetadata.Type type = propMeta.getTypeAsEnum();
            DomibusPropertyValidator validator = type.getValidator();
            if (validator == null) {
                LOG.debug("Validator for type [{}] of property [{}] is null; exiting validation.", propMeta.getType(), propMeta.getName());
                return;
            }

            if (!validator.isValid(propertyValue)) {
                throw new DomibusPropertyException("Property value [" + propertyValue + "] of property [" + propMeta.getName() + "] does not match property type [" + type.name() + "].");
            }
        } catch (IllegalArgumentException ex) {
            LOG.warn("Property type [{}] of property [{}] is not known; exiting validation.", propMeta.getType(), propMeta.getName());
        }
    }

    protected void doSetPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey;
        //calculate property key
        if (domibusConfigurationService.isMultiTenantAware()) {
            // in multi-tenancy mode - some properties will be prefixed (depends on usage)
            propertyKey = computePropertyKeyInMultiTenancy(domain, propertyName);
        } else {
            // in single-tenancy mode - the property key is always the property name
            propertyKey = propertyName;
        }

        //set the value
        domibusPropertyProvider.setValueInDomibusPropertySource(propertyKey, propertyValue);
    }

    protected void signalPropertyValueChanged(Domain domain, String propertyName, String propertyValue, boolean broadcast, DomibusPropertyMetadata propMeta, String oldValue) {
        String domainCode = domain != null ? domain.getCode() : null;
        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        LOG.debug("Property [{}] changed its value on domain [{}], broadcasting is [{}]", propMeta, domainCode, shouldBroadcast);

        try {
            propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
        } catch (DomibusPropertyException ex) {
            LOG.error("An error occurred when executing property change listeners for property [{}]. Reverting to the former value.", propertyName, ex);
            try {
                // revert to old value
                doSetPropertyValue(domain, propertyName, oldValue);
                propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, oldValue, shouldBroadcast);
                throw ex;
            } catch (DomibusPropertyException ex2) {
                LOG.error("An error occurred trying to revert property [{}]. Exiting.", propertyName, ex2);
                throw ex2;
            }
        }
    }

    protected String computePropertyKeyInMultiTenancy(Domain domain, String propertyName) {
        String propertyKey = null;
        DomibusPropertyMetadata prop = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (domain != null) {
            propertyKey = computePropertyKeyForDomain(domain, propertyName, prop);
        } else {
            propertyKey = computePropertyKeyWithoutDomain(propertyName, prop);
        }
        return propertyKey;
    }

    private String computePropertyKeyWithoutDomain(String propertyName, DomibusPropertyMetadata prop) {
        String propertyKey = propertyName;
        if (prop.isSuper()) {
            propertyKey = domibusPropertyProvider.getPropertyKeyForSuper(propertyName);
        } else {
            if (!prop.isGlobal()) {
                String error = String.format("Property %s is not applicable for global usage so it cannot be set.", propertyName);
                throw new DomibusPropertyException(error);
            }
        }
        return propertyKey;
    }

    private String computePropertyKeyForDomain(Domain domain, String propertyName, DomibusPropertyMetadata prop) {
        String propertyKey;
        if (prop.isDomain()) {
            propertyKey = domibusPropertyProvider.getPropertyKeyForDomain(domain, propertyName);
        } else {
            String error = String.format("Property %s is not applicable for a specific domain so it cannot be set.", propertyName);
            throw new DomibusPropertyException(error);
        }
        return propertyKey;
    }
}
