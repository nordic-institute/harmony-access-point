package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The single entry point for getting and setting internal and external domibus properties;
 * It acts also like an aggregator of services like decryption, dispatching to external modules, etc
 *
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class DomibusPropertyProviderImpl implements DomibusPropertyProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyProviderImpl.class);

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final PropertyProviderDispatcher propertyProviderDispatcher;

    private final PrimitivePropertyTypesManager primitivePropertyTypesManager;

    private final NestedPropertiesManager nestedPropertiesManager;

    private final ConfigurableEnvironment environment;

    private final PropertyProviderHelper propertyProviderHelper;

    private final PasswordDecryptionService passwordDecryptionService;

    public DomibusPropertyProviderImpl(GlobalPropertyMetadataManager globalPropertyMetadataManager, PropertyProviderDispatcher propertyProviderDispatcher,
                                       PrimitivePropertyTypesManager primitivePropertyTypesManager, NestedPropertiesManager nestedPropertiesManager,
                                       ConfigurableEnvironment environment, PropertyProviderHelper propertyProviderHelper,
                                       PasswordDecryptionService passwordDecryptionService) {
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderDispatcher = propertyProviderDispatcher;
        this.primitivePropertyTypesManager = primitivePropertyTypesManager;
        this.nestedPropertiesManager = nestedPropertiesManager;
        this.environment = environment;
        this.propertyProviderHelper = propertyProviderHelper;
        this.passwordDecryptionService = passwordDecryptionService;
    }

    @Override
    public String getProperty(String propertyName) throws DomibusPropertyException {
        return getPropertyValue(propertyName, null);
    }

    @Override
    public String getProperty(Domain domain, String propertyName) throws DomibusPropertyException {
        if (domain == null) {
            throw new DomibusPropertyException("Property " + propertyName + " cannot be retrieved without a domain");
        }

        return getPropertyValue(propertyName, domain);
    }

    @Override
    public Integer getIntegerProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getIntegerInternal(propertyName, value);
    }

    @Override
    public Long getLongProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getLongInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(Domain domain, String propertyName) {
        String domainValue = getProperty(domain, propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, domainValue);
    }

    @Override
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return propertyProviderHelper.filterPropertyNames(predicate);
    }

    @Override
    public List<String> getNestedProperties(Domain domain, String prefix) {
        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(prefix);
        return nestedPropertiesManager.getNestedProperties(domain, propertyMetadata);
    }

    @Override
    public List<String> getNestedProperties(String prefix) {
        return getNestedProperties(null, prefix);
    }

    @Override
    public boolean containsDomainPropertyKey(Domain domain, String propertyName) {
        final String domainPropertyName = propertyProviderHelper.getPropertyKeyForDomain(domain, propertyName);
        boolean domainPropertyKeyFound = environment.containsProperty(domainPropertyName);
        if (!domainPropertyKeyFound) {
            domainPropertyKeyFound = environment.containsProperty(propertyName);
        }
        return domainPropertyKeyFound;
    }

    @Override
    public boolean containsPropertyKey(String propertyName) {
        return environment.containsProperty(propertyName);
    }

    @Override
    public void setProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException {
        propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException {
        propertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
    }

    @Override
    public void setProperty(Domain domain, String propertyName, String propertyValue) throws DomibusPropertyException {
        setProperty(domain, propertyName, propertyValue, true);
    }

    protected String getPropertyValue(String propertyName, Domain domain) {
        String result = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);

        DomibusPropertyMetadata meta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (meta.isEncrypted() && passwordDecryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}]", propertyName);
            result = passwordDecryptionService.decryptProperty(domain, propertyName, result);
        }

        return result;
    }
}
