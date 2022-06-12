package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

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

    protected abstract String getEncryptedPropertyNames();

    protected abstract Optional<String> getConfigurationFileName();

    @Override
    public abstract DomainDTO getDomain();

    @Override
    public String getProperty(String propertyName) {
        return propertyProvider.getKnownPropertyValue(propertyName);
    }

    @Override
    public File getConfigurationFile() {
        Optional<String> configurationFileName = getConfigurationFileName();
        return configurationFileName.map(fileName -> {
            String filePath = domibusConfigurationExtService.getConfigLocation() + File.separator + fileName;
            final File configurationFile = new File(filePath);
            LOG.debug("Using configuration file [{}]", configurationFile);
            return configurationFile;
        }).orElse(null);
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        return pluginPasswordEncryptionService.getPropertiesToEncrypt(getEncryptedPropertyNames(), this::getProperty);
    }

}
