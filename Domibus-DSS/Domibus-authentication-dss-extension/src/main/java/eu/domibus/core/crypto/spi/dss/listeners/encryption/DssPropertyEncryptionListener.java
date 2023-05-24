package eu.domibus.core.crypto.spi.dss.listeners.encryption;

import eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.encryption.PluginPropertyEncryptionListener;
import org.springframework.stereotype.Service;

/**
 * @author Soumya Chandran
 * @since 5.0
 *
 * Listener for DSS Password encryption property change.
 */
@Service
public class DssPropertyEncryptionListener extends PluginPropertyEncryptionListenerAbstract
        implements PluginPropertyEncryptionListener {

    protected DssExtensionPropertyManager propertyProvider;

    public DssPropertyEncryptionListener(DssExtensionPropertyManager propertyProvider,
                                              PasswordEncryptionExtService pluginPasswordEncryptionService,
                                              DomibusConfigurationExtService domibusConfigurationExtService) {
        super(pluginPasswordEncryptionService, domibusConfigurationExtService);

        this.propertyProvider = propertyProvider;
    }

    @Override
    protected PluginPasswordEncryptionContext getDomainPasswordEncryptionContextDomain(DomainDTO domain) {
        return new DssDomainPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService, domain);
    }

    @Override
    protected PluginPasswordEncryptionContext getGlobalPasswordEncryptionContext() {
        return new DssGlobalPasswordEncryptionContext(propertyProvider, domibusConfigurationExtService, pluginPasswordEncryptionService);
    }

}
