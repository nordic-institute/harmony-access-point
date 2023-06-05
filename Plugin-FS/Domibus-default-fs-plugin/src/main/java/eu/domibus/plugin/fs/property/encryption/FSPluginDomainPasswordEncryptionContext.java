package eu.domibus.plugin.fs.property.encryption;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Optional;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.PASSWORD_ENCRYPTION_ACTIVE;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class FSPluginDomainPasswordEncryptionContext extends PluginPasswordEncryptionContextAbstract implements PluginPasswordEncryptionContext {

    protected DomainDTO domain;

    public FSPluginDomainPasswordEncryptionContext(DomibusPropertyManagerExt propertyProvider,
                                                   DomibusConfigurationExtService domibusConfigurationExtService,
                                                   PasswordEncryptionExtService pluginPasswordEncryptionService,
                                                   DomainDTO domain) {
        super(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
        this.domain = domain;
    }

    @Override
    public DomainDTO getDomain() {
        return domain;
    }

    @Override
    public String getProperty(String propertyName) {
        return propertyProvider.getKnownPropertyValue(domain.getCode(), propertyName);
    }

    @Override
    public boolean isEncryptionActive() {
        final String passwordEncryptionActive = propertyProvider.getKnownPropertyValue(domain.getCode(), PASSWORD_ENCRYPTION_ACTIVE);
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }

    @Override
    protected String getEncryptedPropertyNames() {
        return FSPLUGIN_PASSWORD_ENCRYPTION_PROPERTIES;
    }

    @Override
    protected Optional<String> getConfigurationFileName() {
        return propertyProvider.getConfigurationFileName(domain);
    }
}
