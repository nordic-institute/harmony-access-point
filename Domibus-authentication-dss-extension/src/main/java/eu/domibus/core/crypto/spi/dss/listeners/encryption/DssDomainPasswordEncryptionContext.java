package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Optional;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public class DssDomainPasswordEncryptionContext extends PluginPasswordEncryptionContextAbstract implements PluginPasswordEncryptionContext {

    protected DomainDTO domain;

    public DssDomainPasswordEncryptionContext(DomibusPropertyManagerExt propertyProvider,
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
    protected String getEncryptedPropertyNames() {
        return DssExtensionPropertyManager.AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_PROPERTIES;
    }

    @Override
    protected Optional<String> getConfigurationFileName() {
        return propertyProvider.getConfigurationFileName(domain);
    }

    @Override
    public boolean isEncryptionActive() {
        final String passwordEncryptionActive = propertyProvider.getKnownPropertyValue(domain.getCode(), AUTHENTICATION_DSS_PASSWORD_ENCRYPTION_ACTIVE);
        return BooleanUtils.toBoolean(passwordEncryptionActive);
    }
}
