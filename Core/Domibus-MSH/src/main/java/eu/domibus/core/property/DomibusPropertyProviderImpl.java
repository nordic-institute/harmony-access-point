package eu.domibus.core.property;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;
import static eu.domibus.api.property.DomibusPropertyProvider.SPRING_BEAN_NAME;

/**
 * The single entry point for getting and setting internal and external domibus properties;
 * It acts also like an aggregator of services like decryption, dispatching to external modules, etc
 *
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
@Service(SPRING_BEAN_NAME)
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

    private final DomibusLocalCacheService domibusLocalCacheService;

    public DomibusPropertyProviderImpl(GlobalPropertyMetadataManager globalPropertyMetadataManager, PropertyProviderDispatcher propertyProviderDispatcher,
                                       PrimitivePropertyTypesManager primitivePropertyTypesManager, NestedPropertiesManager nestedPropertiesManager,
                                       ConfigurableEnvironment environment, PropertyProviderHelper propertyProviderHelper,
                                       PasswordDecryptionService passwordDecryptionService, AnnotationConfigWebApplicationContext rootContext,
                                       DomibusConfigurationService domibusConfigurationService, DomibusLocalCacheService domibusLocalCacheService) {
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderDispatcher = propertyProviderDispatcher;
        this.primitivePropertyTypesManager = primitivePropertyTypesManager;
        this.nestedPropertiesManager = nestedPropertiesManager;
        this.environment = environment;
        this.propertyProviderHelper = propertyProviderHelper;
        this.passwordDecryptionService = passwordDecryptionService;
        this.rootContext = rootContext;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusLocalCacheService = domibusLocalCacheService;
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
        checkIntegerProperty(propertyName);
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getIntegerInternal(propertyName, value);
    }

    @Override
    public Long getLongProperty(String propertyName) {
        checkLongProperty(propertyName);
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getLongInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(String propertyName) {
        checkBooleanProperty(propertyName);
        String value = getProperty(propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, value);
    }

    @Override
    public Boolean getBooleanProperty(Domain domain, String propertyName) {
        checkBooleanProperty(propertyName);
        String domainValue = getProperty(domain, propertyName);
        return primitivePropertyTypesManager.getBooleanInternal(propertyName, domainValue);
    }

    @Override
    @Deprecated
    public Set<String> filterPropertiesName(Predicate<String> predicate) {
        return filterPropertyNames(predicate);
    }

    @Override
    public Set<String> filterPropertyNames(Predicate<String> predicate) {
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
        if (StringUtils.equals(propertyValue, getProperty(domain, propertyName))) {
            LOG.info("The property [{}] has already the value [{}] on domain [{}]; exiting.", propertyName, propertyValue, domain);
            return;
        }
        propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException {
        if (StringUtils.equals(propertyValue, getProperty(propertyName))) {
            LOG.info("The property [{}] has already the value [{}] on domain [{}]; exiting.", propertyName, propertyValue, null);
            return;
        }
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

    @Override
    public void loadProperties(Domain domain) {
        String configurationFileName = domibusConfigurationService.getConfigurationFileName(domain);
        loadProperties(domain, configurationFileName);
    }

    @Override
    public void loadProperties(Domain domain, String propertiesFilePath) {
        if (StringUtils.isEmpty(propertiesFilePath)) {
            LOG.info("Exiting loading properties file for domain [{}] as properties file path is empty.", domain);
            return;
        }
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        String configFile = domibusConfigurationService.getConfigLocation() + File.separator + propertiesFilePath;
        LOG.debug("Loading properties file for domain [{}]: [{}]...", domain, configFile);
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Properties properties = new Properties();
            properties.load(fis);
            String propertySourceName = getSourceName(configFile);
            DomibusPropertiesPropertySource newPropertySource = new DomibusPropertiesPropertySource(propertySourceName, properties);
            propertySources.addFirst(newPropertySource);
        } catch (IOException ex) {
            throw new ConfigurationException(String.format("Could not read properties file: [%s] for domain [%s]", configFile, domain), ex);
        }

        //need this eviction since the load properties puts an empty value to domain title
        domibusLocalCacheService.evict(DomibusLocalCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderHelper.getCacheKeyValue(domain, globalPropertyMetadataManager.getPropertyMetadata(DOMAIN_TITLE)));
        domain.setName(getDomainTitle(domain));
    }

    @Override
    public void removeProperties(Domain domain) {
        removeProperties(domain, domibusConfigurationService.getConfigurationFileName(domain));
    }

    @Override
    public void removeProperties(Domain domain, String propertiesFilePath) {
        if (StringUtils.isEmpty(propertiesFilePath)) {
            LOG.info("Exiting removing for domain [{}] as properties file path is empty.", domain);
            return;
        }
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        String configFile = domibusConfigurationService.getConfigLocation() + File.separator + propertiesFilePath;
        LOG.debug("Removing properties file for domain [{}]: [{}].", domain, configFile);
        String propertySourceName = getSourceName(configFile);
        propertySources.remove(propertySourceName);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.DOMIBUS_PROPERTY_CACHE);
    }

    @Override
    public List<String> getCommaSeparatedPropertyValues(String propertyName) {
        DomibusPropertyMetadata.Type propertyType = getPropertyType(propertyName);
        if (propertyType != DomibusPropertyMetadata.Type.COMMA_SEPARATED_LIST) {
            LOG.debug("Cannot get the individual parts for property [{}] because its type [{}] is not a comma separated list one", propertyName, propertyType);
            throw new DomibusPropertyException("Cannot get the individual parts for property " + propertyName
                    + " because its type " + propertyType + " is not a comma separated list one");
        }

        String propertyValue = getProperty(propertyName);
        return Arrays.stream(StringUtils.split(StringUtils.trimToEmpty(propertyValue), ','))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private String getSourceName(String configFile) {
        return new File(configFile).getName();
    }

    protected String getPropertyValue(String propertyName, Domain domain) {
        String value = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);

        return decryptIfApplicable(propertyName, domain, value);
    }

    protected String getRawPropertyValue(String propertyName, Domain domain) {
        return propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
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

    private void checkIntegerProperty(String propertyName) {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (!propMeta.getTypeAsEnum().isNumeric()) {
            throw new DomibusPropertyException(String.format("Cannot call getIntegerProperty because property [%s] has [%s] type.", propertyName, propMeta.getType()));
        }
    }

    private void checkLongProperty(String propertyName) {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (!propMeta.getTypeAsEnum().isNumeric()) {
            throw new DomibusPropertyException(String.format("Cannot call getLongProperty because property [%s] has [%s] type.", propertyName, propMeta.getType()));
        }
    }

    private void checkBooleanProperty(String propertyName) {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (!propMeta.getTypeAsEnum().isBoolean()) {
            throw new DomibusPropertyException(String.format("Cannot call getBooleanProperty because property [%s] has [%s] type.", propertyName, propMeta.getType()));
        }
    }
}
