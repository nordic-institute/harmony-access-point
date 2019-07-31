package eu.domibus.core.property.encryption.plugin;

import eu.domibus.api.property.encryption.PasswordEncryptionContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.ext.services.PluginPasswordEncryptionContext;

import java.io.File;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class PluginPasswordEncryptionContextDelegate implements PasswordEncryptionContext {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginPasswordEncryptionContextDelegate.class);

    protected PluginPasswordEncryptionContext pluginPasswordEncryptionContext;
    protected PasswordEncryptionContext domibusEncryptionContext;

    public PluginPasswordEncryptionContextDelegate(PluginPasswordEncryptionContext pluginPasswordEncryptionContext,
                                                   PasswordEncryptionContext domibusEncryptionContext) {
        this.pluginPasswordEncryptionContext = pluginPasswordEncryptionContext;
        this.domibusEncryptionContext = domibusEncryptionContext;
    }

    @Override
    public boolean isPasswordEncryptionActive() {
        final boolean domibusEncryptionActive = domibusEncryptionContext.isPasswordEncryptionActive();
        LOG.debug("domibusEncryptionActive? [{}]", domibusEncryptionActive);
        return domibusEncryptionActive;
    }

    @Override
    public String getProperty(String propertyName) {
        LOG.debug("Getting property value for [{}]", propertyName);
        return pluginPasswordEncryptionContext.getProperty(propertyName);
    }

    @Override
    public File getConfigurationFile() {
        final File configurationFile = pluginPasswordEncryptionContext.getConfigurationFile();
        LOG.debug("Getting configuration file [{}]", configurationFile);
        return configurationFile;
    }

    @Override
    public List<String> getPropertiesToEncrypt() {
        final List<String> propertiesToEncrypt = pluginPasswordEncryptionContext.getPropertiesToEncrypt();
        LOG.debug("Getting properties to encrypt [{}]", propertiesToEncrypt);
        return propertiesToEncrypt;
    }

    @Override
    public File getEncryptedKeyFile() {
        final File encryptedKeyFile = domibusEncryptionContext.getEncryptedKeyFile();
        LOG.debug("Using encryptedKeyFile [{}]", encryptedKeyFile);
        return encryptedKeyFile;
    }
}
