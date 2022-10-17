package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.util.backup.BackupService;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.api.property.Module.MSH;
import static eu.domibus.core.property.encryption.PasswordEncryptionServiceImpl.PROPERTY_VALUE_DELIMITER;

/**
 * Responsible for changing the values of domibus properties
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PropertyChangeManager {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyChangeManager.class);

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final PropertyRetrieveManager propertyRetrieveManager;

    private final DomibusPropertyChangeNotifier propertyChangeNotifier;

    private final PropertyProviderHelper propertyProviderHelper;

    private final ConfigurableEnvironment environment;

    private final DomibusCacheService domibusCacheService;

    private final DomibusConfigurationService domibusConfigurationService;

    private final BackupService backupService;

    private final DomibusCoreMapper coreMapper;

    public PropertyChangeManager(GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                 PropertyRetrieveManager propertyRetrieveManager,
                                 PropertyProviderHelper propertyProviderHelper,
                                 ConfigurableEnvironment environment,
                                 // needs to be lazy because we do have a conceptual cyclic dependency:
                                 // BeanX->PropertyProvider->PropertyChangeManager->PropertyChangeNotifier->PropertyChangeListenerX->BeanX
                                 @Lazy DomibusPropertyChangeNotifier propertyChangeNotifier, DomibusCacheService domibusCacheService,
                                 DomibusConfigurationService domibusConfigurationService, BackupService backupService, DomibusCoreMapper coreMapper) {
        this.propertyRetrieveManager = propertyRetrieveManager;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderHelper = propertyProviderHelper;
        this.environment = environment;
        this.propertyChangeNotifier = propertyChangeNotifier;
        this.domibusCacheService = domibusCacheService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.backupService = backupService;
        this.coreMapper = coreMapper;
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
            return propertyRetrieveManager.getInternalProperty(propertyName);
        }
        return propertyRetrieveManager.getInternalProperty(domain, propertyName);
    }

    protected void doSetPropertyValue(Domain domain, String propertyName, String propertyValue) {
        String propertyKey;
        //calculate property key
        if (propertyProviderHelper.isMultiTenantAware()) {
            // in multi-tenancy mode - some properties will be prefixed (depends on usage)
            propertyKey = computePropertyKeyInMultiTenancy(domain, propertyName);
        } else {
            // in single-tenancy mode - the property key is always the property name
            propertyKey = propertyName;
        }

        //set the value
        setValueInDomibusPropertySource(propertyKey, propertyValue);

        saveInFile(domain, propertyName, propertyValue, propertyKey);
    }

    private void saveInFile(Domain domain, String propertyName, String propertyValue, String propertyKey) {
        String configurationFileName = getConfigurationFileName(domain, propertyName);
        String configFileName = domibusConfigurationService.getConfigLocation() + File.separator + configurationFileName;
        File configurationFile = new File(configFileName);
        replacePropertyInFile(configurationFile, propertyKey, propertyValue);
    }

    private String getConfigurationFileName(Domain domain, String propertyName) {
        String configurationFileName = null;
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (StringUtils.equals(propMeta.getModule(), MSH)) {
            configurationFileName = getDomibusPropertyFileName(domain, configurationFileName, propMeta);
        } else {
            configurationFileName = getExternalModulePropertyFileName(domain, propertyName, configurationFileName, propMeta);
        }
        return configurationFileName;
    }

    private String getDomibusPropertyFileName(Domain domain, String configurationFileName, DomibusPropertyMetadata propMeta) {
        if (propertyProviderHelper.isMultiTenantAware()) {
            if (domain != null) {
                if (propMeta.isDomain()) {
                    configurationFileName = domibusConfigurationService.getConfigurationFileName(domain);
                } else {
                    // error
                }
            } else {
                if (propMeta.isSuper()) {
                    configurationFileName = domibusConfigurationService.getConfigurationFileNameForSuper();
                } else if (propMeta.isGlobal()) {
                    configurationFileName = domibusConfigurationService.getConfigurationFileName();
                } else {
                    // error
                }
            }
        } else {
            configurationFileName = domibusConfigurationService.getConfigurationFileName();
        }
        return configurationFileName;
    }

    private String getExternalModulePropertyFileName(Domain domain, String propertyName, String configurationFileName, DomibusPropertyMetadata propMeta) {
        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
        if (propertyProviderHelper.isMultiTenantAware()) {
            if (domain != null) {
                DomainDTO extDomain = coreMapper.domainToDomainDTO(domain);
                if (propMeta.isDomain()) {
                    configurationFileName = manager.getConfigurationFileName(extDomain).get();
                } else {
                    // error
                }
            } else {
                if (propMeta.isGlobal()) {
                    configurationFileName = manager.getConfigurationFileName();
                } else {
                    // error
                }
            }
        } else {
            configurationFileName = manager.getConfigurationFileName();
        }
        return configurationFileName;
    }

    protected void signalPropertyValueChanged(Domain domain, String propertyName, String propertyValue,
                                              boolean broadcast, DomibusPropertyMetadata propMeta, String oldValue) {
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
                //clear the cache manually here since we are not calling the set method through dispatcher class
                domibusCacheService.evict(DomibusCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderHelper.getCacheKeyValue(domain, propMeta));
                // the original property set failed likely due to the change listener validation so, there is no side effect produced and no need to call the listener again
//                propertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, oldValue, shouldBroadcast);
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
            propertyKey = propertyProviderHelper.getPropertyKeyForSuper(propertyName);
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
            propertyKey = propertyProviderHelper.getPropertyKeyForDomain(domain, propertyName);
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

    protected void replacePropertyInFile(File configurationFile, String propertyName, String newPropertyValue) {
        final List<String> replacedLines = getReplacedLines(configurationFile, propertyName, newPropertyValue);
        String newLine = getPropertyNameValueLine(propertyName, newPropertyValue);
        if (!replacedLines.contains(newLine)) {
            replacedLines.add(newLine);
        }

        try {
            backupService.backupFile(configurationFile);
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not back up [%s]", configurationFile), e);
        }

        try {
            Files.write(configurationFile.toPath(), replacedLines);
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not write property [{}] to file [%s] ", propertyName, configurationFile), e);
        }
    }

    private String getPropertyNameValueLine(String propertyName, String newPropertyValue) {
        return propertyName + PROPERTY_VALUE_DELIMITER + newPropertyValue;
    }

    protected List<String> getReplacedLines(File configurationFile, String propertyName, String newPropertyValue) {
        try (final Stream<String> lines = Files.lines(configurationFile.toPath())) {
            return lines
                    .map(line -> replaceLine(line, propertyName, newPropertyValue))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }
    }

    protected String replaceLine(String line, String propertyName, String newPropertyValue) {
        if (!arePropertiesMatching(line, propertyName)) {
            return line;
        }

        return getPropertyNameValueLine(propertyName, newPropertyValue);
    }

    protected boolean arePropertiesMatching(String filePropertyName, String propertyName) {
        return StringUtils.contains(filePropertyName, propertyName);
    }

}
