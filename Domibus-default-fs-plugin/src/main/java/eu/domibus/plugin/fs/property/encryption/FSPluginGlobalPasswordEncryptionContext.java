package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class FSPluginGlobalPasswordEncryptionContext extends PluginPasswordEncryptionContextAbstract implements PluginPasswordEncryptionContext {

    public FSPluginGlobalPasswordEncryptionContext(DomibusPropertyManagerExt propertyProvider,
                                                   DomibusConfigurationExtService domibusConfigurationExtService,
                                                   PasswordEncryptionExtService pluginPasswordEncryptionService) {
        super(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
    }

    @Override
    public DomainDTO getDomain() {
        return null;
    }

    @Override
    public String getProperty(String propertyName) {
        return propertyProvider.getKnownPropertyValue(propertyName);
    }

    @Override
    protected String getEncryptedPropertyNames() {
        return FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
    }

}
