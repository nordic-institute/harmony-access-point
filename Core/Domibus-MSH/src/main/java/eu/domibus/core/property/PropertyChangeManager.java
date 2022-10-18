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

import static eu.domibus.api.property.Module.MSH;
import static eu.domibus.api.property.Module.UNKNOWN;
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
                String error = String.format("Property [%s] is not applicable for global usage so it cannot be set.", propertyName);
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
            String error = String.format("Property [%s] is not applicable for a specific domain so it cannot be set.", propertyName);
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

    private void saveInFile(Domain domain, String propertyName, String propertyValue, String propertyKey) {
        String configurationFileName = getConfigurationFileName(domain, propertyName);
        String fullName = domibusConfigurationService.getConfigLocation() + File.separator + configurationFileName;
        replacePropertyInFile(new File(fullName), propertyKey, propertyValue);
    }

    protected String getConfigurationFileName(Domain domain, String propertyName) {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (StringUtils.equalsAny(propMeta.getModule(), MSH, UNKNOWN)) {
            LOG.debug("Domibus property [{}] on domain [{}].", propertyName, domain);
            return getDomibusPropertyFileName(domain, propMeta);
        } else {
            DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
            LOG.debug("External module [{}] property [{}] on domain [{}].", manager.getClass(), propertyName, domain);
            return getExternalModulePropertyFileName(domain, manager, propMeta);
        }
    }

    private String getDomibusPropertyFileName(Domain domain, DomibusPropertyMetadata propMeta) {
        if (!propertyProviderHelper.isMultiTenantAware()) {
            String configurationFileName = domibusConfigurationService.getConfigurationFileName();
            LOG.debug("Properties file name in single-tenancy mode for property [{}] is [{}].", propMeta.getName(), configurationFileName);
            return configurationFileName;
        }
        if (domain != null) {
            if (propMeta.isDomain()) {
                String configurationFileName = domibusConfigurationService.getConfigurationFileName(domain);
                LOG.debug("Properties file name in multi-tenancy mode for property [{}] on domain [{}] is [{}].", propMeta.getName(), domain, configurationFileName);
                return configurationFileName;
            } else {
                throw new DomibusPropertyException(String.format("Property [%s] is not applicable for domain usage so it cannot be set.", propMeta.getName()));
            }
        } else {
            if (propMeta.isSuper()) {
                String configurationFileNameForSuper = domibusConfigurationService.getConfigurationFileNameForSuper();
                LOG.debug("Properties file name in multi-tenancy mode for super property [{}] is [{}].", propMeta.getName(), configurationFileNameForSuper);
                return configurationFileNameForSuper;
            } else if (propMeta.isGlobal()) {
                String configurationFileName = domibusConfigurationService.getConfigurationFileName();
                LOG.debug("Properties file name in multi-tenancy mode for global property [{}] is [{}].", propMeta.getName(), configurationFileName);
                return configurationFileName;
            } else {
                throw new DomibusPropertyException(String.format("Property [%s] is not applicable for global or super usage so it cannot be set.", propMeta.getName()));
            }
        }
    }

    private String getExternalModulePropertyFileName(Domain domain, DomibusPropertyManagerExt manager, DomibusPropertyMetadata propMeta) {
        if (!propertyProviderHelper.isMultiTenantAware()) {
            String configurationFileName = manager.getConfigurationFileName();
            LOG.debug("Properties file name in single-tenancy mode for property [{}] is [{}].", propMeta.getName(), configurationFileName);
            return configurationFileName;
        }

        if (domain != null) {
            if (propMeta.isDomain()) {
                DomainDTO extDomain = coreMapper.domainToDomainDTO(domain);
                String configurationFileName = manager.getConfigurationFileName(extDomain)
                        .orElseThrow(() -> new DomibusPropertyException(String.format("Could not find properties file name for external module [%s] on domain [%s].", manager.getClass(), domain)));
                LOG.debug("Properties file name in multi-tenancy mode for property [{}] on domain [{}] is [{}].", propMeta.getName(), domain, configurationFileName);
                return configurationFileName;
            } else {
                throw new DomibusPropertyException(String.format("Property [%s] is not applicable for domain usage so it cannot be set.", propMeta.getName()));
            }
        } else {
            if (propMeta.isGlobal()) {
                String configurationFileName = manager.getConfigurationFileName();
                LOG.debug("Properties file name in multi-tenancy mode for global property [{}] is [{}].", propMeta.getName(), configurationFileName);
                return configurationFileName;
            } else {
                throw new DomibusPropertyException(String.format("Property [%s] is not applicable for global usage so it cannot be set.", propMeta.getName()));
            }
        }
    }

    protected void replacePropertyInFile(File configurationFile, String propertyName, String newPropertyValue) {
        final List<String> lines = replaceOrAddProperty(configurationFile, propertyName, newPropertyValue);

        try {
            backupService.backupFile(configurationFile);
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not back up [%s]", configurationFile), e);
        }

        try {
            Files.write(configurationFile.toPath(), lines);
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not write property [%s] to file [%s] ", propertyName, configurationFile), e);
        }
    }

    protected List<String> replaceOrAddProperty(File configurationFile, String propertyName, String newPropertyValue) {
        String propertyNameValueLine = propertyName + PROPERTY_VALUE_DELIMITER + newPropertyValue;
        try {
            List<String> lines = Files.readAllLines(configurationFile.toPath());
            // to make sure we do not replace a property that lists other props (like encryption.property)
            String valueToSearch = propertyName + "=";
            // go backwards so that, in case there are more than one line with the same property, replace the last one to take precedence
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (StringUtils.contains(lines.get(i), valueToSearch)) {
                    // found it, replace and exit
                    lines.set(i, propertyNameValueLine);
                    return lines;
                }
            }
            // could not find, so just add at the end
            lines.add(propertyNameValueLine);
            return lines;
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }
    }

}
