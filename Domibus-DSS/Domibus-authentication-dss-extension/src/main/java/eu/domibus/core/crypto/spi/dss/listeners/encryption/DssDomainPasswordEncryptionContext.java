package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;

import java.util.Optional;

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
        // encrypt password active property is global now, so we cannot call get property value on a domain
        return false;
    }
}
