package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Responsible with getting the domibus properties that can be changed at runtime, getting and setting their values
 */

@Service(DomibusPropertyManager.MSH_PROPERTY_MANAGER)
public class DomibusPropertyManagerImpl implements DomibusPropertyManager {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyManagerImpl.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Autowired
    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    /**
     * Returns the properties that this PropertyProvider is able to handle.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return domibusPropertyMetadataManager.getKnownProperties();
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return domibusPropertyMetadataManager.hasKnownProperty(name);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        checkPropertyExists(propertyName);

        Domain domain = domainCode == null ? null : domainService.getDomain(domainCode);
        return domibusPropertyProvider.getProperty(domain, propertyName);
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        checkPropertyExists(propertyName);

        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        Domain domain = domainCode != null ? domainService.getDomain(domainCode) : null;
        this.setPropertyValue(domain, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) throws DomibusPropertyException {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        this.setPropertyValue(domain, propertyName, propertyValue, true);
    }

    protected void setPropertyValue(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        DomibusPropertyMetadata propMeta = this.getKnownProperties().get(propertyName);
        if (propMeta == null) {
            throw new DomibusPropertyException("Property " + propertyName + " not found.");
        }

        String oldValue = domibusPropertyProvider.getProperty(domain, propertyName);
        domibusPropertyProvider.setPropertyValue(domain, propertyName, propertyValue);

        String domainCode = domain != null ? domain.getCode() : null;
        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        try {
            propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
        } catch (DomibusPropertyException ex) {
            LOGGER.error("An error occurred when executing property change listeners for property [{}]. Reverting to the former value.", propertyName, ex);
            try {
                // revert to old value
                domibusPropertyProvider.setPropertyValue(domain, propertyName, oldValue);
                propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, oldValue, shouldBroadcast);
                // propagate the exception to the client
                throw ex;
            } catch (DomibusPropertyException ex2) {
                LOGGER.error("An error occurred trying to revert property [{}]. Exiting.", propertyName, ex2);
                // failed to revert!!! just report the error
                throw ex2;
            }
        }
    }

    private void checkPropertyExists(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyException("Property " + propertyName + " not found.");
        }
    }

}
