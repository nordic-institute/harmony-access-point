package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public abstract class PluginPasswordEncryptionContextAbstract implements PluginPasswordEncryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginPasswordEncryptionContextAbstract.class);

    protected DomibusPropertyManagerExt propertyProvider;

    protected DomibusConfigurationExtService domibusConfigurationExtService;

    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    public PluginPasswordEncryptionContextAbstract(DomibusPropertyManagerExt propertyProvider,
                                                   DomibusConfigurationExtService domibusConfigurationExtService,
                                                   PasswordEncryptionExtService pluginPasswordEncryptionService) {
        this.propertyProvider = propertyProvider;
        this.domibusConfigurationExtService = domibusConfigurationExtService;
        this.pluginPasswordEncryptionService = pluginPasswordEncryptionService;
    }

    @Override
    public abstract DomainDTO getDomain();

    protected abstract String getEncryptedPropertyNames();

    @Override
    public String getProperty(String propertyName) {
        return propertyProvider.getKnownPropertyValue(propertyName);
    }

    @Override
    public File getConfigurationFile() {
        String filePath = domibusConfigurationExtService.getConfigLocation() + File.separator + propertyProvider.getConfigurationFileName();
//        String filePath = domibusConfigurationExtService.getConfigLocation() + File.separator + PLUGINS_CONFIG_HOME + File.separator + PLUGIN_PROPERTIES_FILE_NAME;
        final File configurationFile = new File(filePath);
        LOG.debug("Using FS Plugin configuration file [{}]", configurationFile);
        return configurationFile;
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        final String propertiesToEncryptString = propertyProvider.getKnownPropertyValue(getEncryptedPropertyNames());

        if (StringUtils.isEmpty(propertiesToEncryptString)) {
            LOG.debug("No properties to encrypt");
            return new ArrayList<>();
        }
        final String[] propertiesToEncrypt = StringUtils.split(propertiesToEncryptString, ",");
        LOG.debug("The following properties are configured for encryption [{}]", Arrays.asList(propertiesToEncrypt));

        List<String> result = Arrays.stream(propertiesToEncrypt).filter(propertyName -> {
            propertyName = StringUtils.trim(propertyName);
            final String propertyValue = getProperty(propertyName);
            if (StringUtils.isBlank(propertyValue)) {
                return false;
            }

            if (!pluginPasswordEncryptionService.isValueEncrypted(propertyValue)) {
                LOG.debug("Property [{}] is not encrypted", propertyName);
                return true;
            }
            LOG.debug("Property [{}] is already encrypted", propertyName);
            return false;
        }).collect(Collectors.toList());

        LOG.debug("The following properties are not encrypted [{}]", result);

        return result;
    }

}
