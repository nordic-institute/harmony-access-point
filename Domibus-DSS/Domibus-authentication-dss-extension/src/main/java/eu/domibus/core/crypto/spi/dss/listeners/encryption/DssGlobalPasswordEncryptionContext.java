package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Optional;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DssGlobalPasswordEncryptionContext extends PluginPasswordEncryptionContextAbstract
        implements PluginPasswordEncryptionContext {

    public DssGlobalPasswordEncryptionContext(DomibusPropertyManagerExt propertyProvider,
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
        return DssExtensionPropertyManager.AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_PROPERTIES;
    }

    @Override
    protected Optional<String> getConfigurationFileName() {
        return Optional.of(propertyProvider.getConfigurationFileName());
    }

    @Override
    public boolean isEncryptionActive() {
        final String passwordEncryptionActive = propertyProvider.getKnownPropertyValue(AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE);
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }
}
