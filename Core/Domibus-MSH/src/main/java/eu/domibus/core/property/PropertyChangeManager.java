package eu.domibus.core.property;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.RegexUtil;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_BACKUP_HISTORY_MAX;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_BACKUP_PERIOD_MIN;
import static eu.domibus.api.property.Module.MSH;
import static eu.domibus.api.property.Module.UNKNOWN;

/**
 * Responsible for changing the values of domibus properties
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PropertyChangeManager {

    public static final String LINE_COMMENT_PREFIX = "#";
    public static final String PROPERTY_VALUE_DELIMITER = "=";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyChangeManager.class);

    private final GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private final PropertyRetrieveManager propertyRetrieveManager;

    private final DomibusPropertyChangeNotifier propertyChangeNotifier;

    private final PropertyProviderHelper propertyProviderHelper;

    private final ConfigurableEnvironment environment;

    private final DomibusLocalCacheService domibusLocalCacheService;

    private final DomibusConfigurationService domibusConfigurationService;

    private final BackupService backupService;

    private final DomibusCoreMapper coreMapper;

    private final RegexUtil regexUtil;

    public PropertyChangeManager(GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                 PropertyRetrieveManager propertyRetrieveManager,
                                 PropertyProviderHelper propertyProviderHelper,
                                 ConfigurableEnvironment environment,
                                 // needs to be lazy because we do have a conceptual cyclic dependency:
                                 // BeanX->PropertyProvider->PropertyChangeManager->PropertyChangeNotifier->PropertyChangeListenerX->BeanX
                                 @Lazy DomibusPropertyChangeNotifier propertyChangeNotifier, DomibusLocalCacheService domibusLocalCacheService,
                                 DomibusConfigurationService domibusConfigurationService, BackupService backupService, DomibusCoreMapper coreMapper, RegexUtil regexUtil) {
        this.propertyRetrieveManager = propertyRetrieveManager;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.propertyProviderHelper = propertyProviderHelper;
        this.environment = environment;
        this.propertyChangeNotifier = propertyChangeNotifier;
        this.domibusLocalCacheService = domibusLocalCacheService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.backupService = backupService;
        this.coreMapper = coreMapper;
        this.regexUtil = regexUtil;
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

    protected String getInternalPropertyValue(Domain domain, String propertyName) {
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
                domibusLocalCacheService.evict(DomibusLocalCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderHelper.getCacheKeyValue(domain, propMeta));
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

    protected void saveInFile(Domain domain, String propertyName, String propertyValue, String propertyKey) {
        File propertyFile = getConfigurationFile(domain, propertyName);
        replacePropertyInFile(propertyFile, propertyKey, propertyValue, domain);
    }

    private File getConfigurationFile(Domain domain, String propertyName) {
        DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        if (StringUtils.equalsAny(propMeta.getModule(), MSH, UNKNOWN)) {
            LOG.debug("Getting property file for MSH property [{}] on domain [{}].", propertyName, domain);
            String domibusPropertyFileName = getDomibusPropertyFileName(domain, propMeta);
            File propertyFile = getFile(domibusPropertyFileName);
            if (!Files.exists(propertyFile.toPath())) {
                throw new DomibusPropertyException(String.format("MSH property file for domain [%s] could not be found at the location [%s]", domain, domibusPropertyFileName));
            }
            return propertyFile;
        } else {
            DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
            LOG.debug("Getting property file of external module [{}] property [{}] on domain [{}].", manager.getClass(), propertyName, domain);
            return getExternalModulePropertyFile(domain, manager, propMeta);
        }
    }

    private String getDomibusPropertyFileName(Domain domain, DomibusPropertyMetadata propMeta) {
        if (!propertyProviderHelper.isMultiTenantAware()) {
            String configurationFileName = domibusConfigurationService.getConfigurationFileName();
            LOG.debug("Properties file name in single-tenancy mode for property [{}] is [{}].", propMeta.getName(), configurationFileName);
            return configurationFileName;
        }
        if (domain != null) {
            return getDomainDomibusPropertyFile(domain, propMeta);
        }
        return getGlobalOrSuperDomibusPropertyFileName(propMeta);
    }

    private String getGlobalOrSuperDomibusPropertyFileName(DomibusPropertyMetadata propMeta) {
        if (propMeta.isSuper()) {
            String configurationFileNameForSuper = domibusConfigurationService.getSuperConfigurationFileName();
            LOG.debug("Properties file name in multi-tenancy mode for super property [{}] is [{}].", propMeta.getName(), configurationFileNameForSuper);
            return configurationFileNameForSuper;
        }
        if (propMeta.isGlobal()) {
            String configurationFileName = domibusConfigurationService.getConfigurationFileName();
            LOG.debug("Properties file name in multi-tenancy mode for global property [{}] is [{}].", propMeta.getName(), configurationFileName);
            return configurationFileName;
        }
        throw new DomibusPropertyException(String.format("Property [%s] is not applicable for global or super usage so it cannot be set.", propMeta.getName()));
    }

    private String getDomainDomibusPropertyFile(Domain domain, DomibusPropertyMetadata propMeta) {
        if (propMeta.isDomain()) {
            String configurationFileName = domibusConfigurationService.getConfigurationFileName(domain);
            LOG.debug("Properties file name in multi-tenancy mode for property [{}] on domain [{}] is [{}].", propMeta.getName(), domain, configurationFileName);
            return configurationFileName;
        }
        throw new DomibusPropertyException(String.format("Property [%s] is not applicable for domain usage so it cannot be set.", propMeta.getName()));
    }

    private File getExternalModulePropertyFile(Domain domain, DomibusPropertyManagerExt manager, DomibusPropertyMetadata propMeta) {
        if (!propertyProviderHelper.isMultiTenantAware()) {
            String configurationFileName = manager.getConfigurationFileName();
            LOG.debug("Properties file name in single-tenancy mode for property [{}] is [{}].", propMeta.getName(), configurationFileName);
            File propertyFile = getFile(configurationFileName);
            if (!Files.exists(propertyFile.toPath())) {
                LOG.info("Properties file for module [{}] could not be found at the location [{}]; creating it now.", propMeta.getName(), configurationFileName);
                try {
                    propertyFile = Files.createFile(propertyFile.toPath()).toFile();
                } catch (IOException e) {
                    throw new DomibusPropertyException(String.format("Could not create the properties file for module [%s] at the location [%s].",
                            propMeta.getName(), domain, configurationFileName), e);
                }
            }
            return propertyFile;
        }

        if (domain != null) {
            return getDomainExternalModulePropertyFile(domain, manager, propMeta);
        }
        return getGlobalExternalModulePropertyFile(manager, propMeta);
    }

    private File getGlobalExternalModulePropertyFile(DomibusPropertyManagerExt manager, DomibusPropertyMetadata propMeta) {
        if (propMeta.isGlobal()) {
            String configurationFileName = manager.getConfigurationFileName();
            LOG.debug("Properties file name in multi-tenancy mode for global property [{}] is [{}].", propMeta.getName(), configurationFileName);
            File propertyFile = getFile(configurationFileName);
            if (!Files.exists(propertyFile.toPath())) {
                throw new DomibusPropertyException(String.format("Global properties file for module [%s] could not be found at the location [%s]",
                        propMeta.getName(), configurationFileName));
            }
            return propertyFile;
        }
        throw new DomibusPropertyException(String.format("Property [%s] is not applicable for global usage so it cannot be set.", propMeta.getName()));
    }

    private File getDomainExternalModulePropertyFile(Domain domain, DomibusPropertyManagerExt manager, DomibusPropertyMetadata propMeta) {
        if (propMeta.isDomain()) {
            DomainDTO extDomain = coreMapper.domainToDomainDTO(domain);
            String configurationFileName = manager.getConfigurationFileName(extDomain)
                    .orElseThrow(() -> new DomibusPropertyException(String.format("Could not find properties file name for external module [%s] on domain [%s].",
                            manager.getClass(), domain)));
            LOG.debug("Properties file name in multi-tenancy mode for property [{}] on domain [{}] is [{}].", propMeta.getName(), domain, configurationFileName);
            File propertyFile = getFile(configurationFileName);
            if (!Files.exists(propertyFile.toPath())) {
                LOG.info("Domain properties file for module [{}] and domain [{}] could not be found at the location [{}]; creating it now.",
                        propMeta.getName(), domain, configurationFileName);
                try {
                    propertyFile = Files.createFile(propertyFile.toPath()).toFile();
                } catch (IOException e) {
                    throw new DomibusPropertyException(String.format("Could not create the domain properties file for module [%s] and domain [%s] at the location [%s].",
                            propMeta.getName(), domain, configurationFileName), e);
                }
            }
            return propertyFile;
        }
        throw new DomibusPropertyException(String.format("Property [%s] is not applicable for domain usage so it cannot be set.", propMeta.getName()));
    }

    private File getFile(String domibusPropertyFileName) {
        String fullName = domibusConfigurationService.getConfigLocation() + File.separator + domibusPropertyFileName;
        return new File(fullName);
    }

    protected void replacePropertyInFile(File configurationFile, String propertyName, String newPropertyValue, Domain domain) {
        final List<String> lines = replaceOrAddProperty(configurationFile, propertyName, newPropertyValue);

        manageBackups(configurationFile, domain);

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
            int i = findLineWithProperty(propertyName, lines);
            if (i >= 0) {
                LOG.debug("Replacing property [{}] in file [{}] with value [{}].", propertyName, configurationFile.getName(), newPropertyValue);
                lines.set(i, propertyNameValueLine);
            } else {
                // could not find, so just add at the end
                LOG.debug("Could not find property [{}] in file [{}] (probably it had a fall-back value set in the global file); adding value [{}] at the end of the file.",
                        propertyName, configurationFile.getName(), newPropertyValue);
                lines.add(propertyNameValueLine);
            }
            return lines;
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not replace properties: could not read configuration file [%s]", configurationFile), e);
        }
    }

    public int findLineWithProperty(String propertyName, List<String> lines) {
        String valueToSearch = LINE_COMMENT_PREFIX + "?" + propertyName + "\\s*" + PROPERTY_VALUE_DELIMITER + ".*";
        // go backwards so that, in case there are more than one line with the same property, replace the last one because it has precedence
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (regexUtil.matches(valueToSearch, StringUtils.trim(line))) {
                // found it, exit
                LOG.debug("Found property [{}] in file.", propertyName);
                return i;
            }
        }
        return -1;
    }

    private void manageBackups(File configurationFile, Domain domain) {
        Integer period = getPropertyValueAsInteger(domain, DOMIBUS_PROPERTY_BACKUP_PERIOD_MIN, 24);
        try {
            backupService.backupFileIfOlderThan(configurationFile,"backups", period);
        } catch (IOException e) {
            throw new DomibusPropertyException(String.format("Could not back up [%s]", configurationFile), e);
        }

        Integer maxFiles = getPropertyValueAsInteger(domain, DOMIBUS_PROPERTY_BACKUP_HISTORY_MAX, 10);
        try {
            backupService.deleteBackupsIfMoreThan(configurationFile, maxFiles);
        } catch (IOException e) {
            LOG.info("Could not delete back up history for [{}] due to [{}]", configurationFile, e);
        }
    }

    protected Integer getPropertyValueAsInteger(Domain domain, String propertyName, int defaultValue) {
        Integer timeout;
        String propVal = null;
        try {
            propVal = getInternalPropertyValue(domain, propertyName);
            timeout = Integer.valueOf(propVal);
        } catch (final NumberFormatException e) {
            LOG.warn("Could not parse the property [{}] value [{}] to an integer; returning [{}].", propertyName, propVal, defaultValue, e);
            timeout = defaultValue;
        }
        return timeout;
    }
}
