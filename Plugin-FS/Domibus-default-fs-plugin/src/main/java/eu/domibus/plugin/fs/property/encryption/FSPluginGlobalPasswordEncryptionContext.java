package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Optional;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.PASSWORD_ENCRYPTION_ACTIVE;

/**
 * @author Ion Perpegel
 * @since 5.0
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

    @Override
    protected Optional<String> getConfigurationFileName() {
        return Optional.of(propertyProvider.getConfigurationFileName());
    }

    @Override
    public boolean isEncryptionActive() {
        // even if there are no domain password properties for now, this is used in ST mode anyway
        return propertyProvider.getKnownBooleanPropertyValue(PASSWORD_ENCRYPTION_ACTIVE);
    }
}
