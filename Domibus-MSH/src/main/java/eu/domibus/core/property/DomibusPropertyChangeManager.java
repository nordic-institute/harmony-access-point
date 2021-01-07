package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

/**
 * Responsible for changing the values of domibus properties
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyChangeManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyChangeManager.class);

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final DomibusPropertyRetrieveManager domibusPropertyRetrieveManager;

    private final DomibusPropertyChangeNotifier propertyChangeNotifier;

    private final DomibusPropertyProviderHelper domibusPropertyProviderHelper;

    private final ConfigurableEnvironment environment;

    public DomibusPropertyChangeManager(GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                        DomibusPropertyRetrieveManager domibusPropertyRetrieveManager,
                                        DomibusPropertyProviderHelper domibusPropertyProviderHelper,
                                        ConfigurableEnvironment environment,
                                        // needs to be lazy because we do have a conceptual cyclic dependency:
                                        // BeanX->PropertyProvider->PropertyChangeManager->PropertyChangeNotifier->PropertyChangeListenerX->BeanX
                                        @Lazy DomibusPropertyChangeNotifier propertyChangeNotifier) {
        this.domibusPropertyRetrieveManager = domibusPropertyRetrieveManager;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.domibusPropertyProviderHelper = domibusPropertyProviderHelper;
        this.environment = environment;
        this.propertyChangeNotifier = propertyChangeNotifier;
    }

    protected void setPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);

        //keep old value in case of an exception
        String oldValue = getInternalPropertyValue(domain, propertyName);

        //try to set the new value
        doSetPropertyValue(domain, propertyName, propertyValue);

        //let the custom property listeners do their job
        signalPropertyValueChanged(domain, propertyName, propertyValue, broadcast, propMeta, oldValue);
    }

    private String getInternalPropertyValue(Domain domain, String propertyName) {
        if (domain == null) {
            return domibusPropertyRetrieveManager.getInternalProperty(propertyName);
        }
        return domibusPropertyRetrieveManager.getInternalProperty(domain, propertyName);
    }

    protected void doSetPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey;
        //calculate property key
        if (domibusPropertyProviderHelper.isMultiTenantAware()) {
            // in multi-tenancy mode - some properties will be prefixed (depends on usage)
            propertyKey = computePropertyKeyInMultiTenancy(domain, propertyName);
        } else {
            // in single-tenancy mode - the property key is always the property name
            propertyKey = propertyName;
        }

        //set the value
        setValueInDomibusPropertySource(propertyKey, propertyValue);
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
            propertyKey = domibusPropertyProviderHelper.getPropertyKeyForSuper(propertyName);
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
            propertyKey = domibusPropertyProviderHelper.getPropertyKeyForDomain(domain, propertyName);
        } else {
            String error = String.format("Property %s is not applicable for a specific domain so it cannot be set.", propertyName);
            throw new DomibusPropertyException(error);
        }
        return propertyKey;
    }

    protected void setValueInDomibusPropertySource(String propertyKey, String propertyValue) {
        MutablePropertySources propertySources = environment.getPropertySources();
        DomibusPropertiesPropertySource domibusPropertiesPropertySource = (DomibusPropertiesPropertySource) propertySources.get(DomibusPropertiesPropertySource.NAME);
        domibusPropertiesPropertySource.setProperty(propertyKey, propertyValue);

        DomibusPropertiesPropertySource updatedDomibusPropertiesSource = (DomibusPropertiesPropertySource) propertySources.get(DomibusPropertiesPropertySource.UPDATED_PROPERTIES_NAME);
        updatedDomibusPropertiesSource.setProperty(propertyKey, propertyValue);
    }
}
