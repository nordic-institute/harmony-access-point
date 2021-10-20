package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;

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

    private final AnnotationConfigWebApplicationContext rootContext;

    private final DomibusConfigurationService domibusConfigurationService;

    private final DomibusCacheService domibusCacheService;

    public DomibusPropertyProviderImpl(GlobalPropertyMetadataManager globalPropertyMetadataManager, PropertyProviderDispatcher propertyProviderDispatcher,
                                       PrimitivePropertyTypesManager primitivePropertyTypesManager, NestedPropertiesManager nestedPropertiesManager,
                                       ConfigurableEnvironment environment, PropertyProviderHelper propertyProviderHelper,
                                       PasswordDecryptionService passwordDecryptionService, AnnotationConfigWebApplicationContext rootContext,
                                       DomibusConfigurationService domibusConfigurationService, DomibusCacheService domibusCacheService) {
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderDispatcher = propertyProviderDispatcher;
        this.primitivePropertyTypesManager = primitivePropertyTypesManager;
        this.nestedPropertiesManager = nestedPropertiesManager;
        this.environment = environment;
        this.propertyProviderHelper = propertyProviderHelper;
        this.passwordDecryptionService = passwordDecryptionService;
        this.rootContext = rootContext;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusCacheService = domibusCacheService;
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

    @Override
    public DomibusPropertyMetadata.Type getPropertyType(String propertyName) {
        DomibusPropertyMetadata meta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        return meta.getTypeAsEnum();
    }

//    @Override
//    public String getConfigurationFileName() {
//        return DOMIBUS_PROPERTY_FILE;
//    }
//
//    @Override
//    public String getConfigurationFileName(Domain domain) {
//        String propertyFileName;
//
//        if (!propertyProviderHelper.isMultiTenantAware()) {
//            propertyFileName = getConfigurationFileName();
//        } else {
//            propertyFileName = getDomainConfigurationFileName(domain);
//        }
//        LOG.debug("Using property file [{}]", propertyFileName);
//
//        return propertyFileName;
//    }
//
//    public String getDomainConfigurationFileName(Domain domain) {
//        return DomainService.DOMAINS_HOME + File.separator + domain.getCode() +
//                File.separator + domain.getCode() + '-' + DOMIBUS_PROPERTY_FILE;
//    }

    @Override
    public void loadProperties(Domain domain) {
        loadProperties(domain, domibusConfigurationService.getConfigurationFileName(domain));

        //need this eviction since the load properties puts an empty value to domain title
        domibusCacheService.evict(DomibusCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderDispatcher.getCacheKeyValue(domain, DOMAIN_TITLE));
        domain.setName(getDomainTitle(domain));
    }

    @Override
    public void loadProperties(Domain domain, String propertiesFilePath) {
        if(StringUtils.isEmpty(propertiesFilePath)){
            LOG.info("Exiting loading properties file for domain [{}] as properties file path is empty. .", domain);
            return;
        }
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        String configFile = domibusConfigurationService.getConfigLocation() + File.separator + propertiesFilePath;
        LOG.debug("Loading properties file for domain [{}]: [{}]...", domain, configFile);
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            DomibusPropertiesPropertySource newPropertySource = new DomibusPropertiesPropertySource("propertiesOfDomain" + domain.getCode(), properties);
            propertySources.addLast(newPropertySource);
        } catch (IOException ex) {
            throw new ConfigurationException(String.format("Could not read properties file: [%s] for domain [%s]", configFile, domain), ex);
        }
    }

    protected String getPropertyValue(String propertyName, Domain domain) {
        String value = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);

        String decryptedValue = decryptIfApplicable(propertyName, domain, value);

        return decryptedValue;
    }

    protected String getRawPropertyValue(String propertyName, Domain domain) {
        String result = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        return result;
    }

    private String decryptIfApplicable(String propertyName, Domain domain, String result) {
        DomibusPropertyMetadata meta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (meta.isEncrypted() && passwordDecryptionService.isValueEncrypted(result)) {
            LOG.debug("Decrypting property [{}]", propertyName);
            result = passwordDecryptionService.decryptProperty(domain, propertyName, result);
        }
        return result;
    }

    private String getDomainTitle(Domain domain) {
        String domainTitle = getProperty(domain, DOMAIN_TITLE);
        if (StringUtils.isEmpty(domainTitle)) {
            domainTitle = domain.getCode();
        }
        return domainTitle;
    }
}
