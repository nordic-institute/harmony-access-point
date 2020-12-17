package eu.domibus.core.alerts.configuration.password.imminent.plugin;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of plugin user password imminent expiration alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginPasswordImminentExpirationAlertConfigurationManager
        extends ReaderAlertConfigurationManager<PasswordExpirationAlertModuleConfiguration, PluginPasswordImminentExpirationAlertConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private PluginPasswordImminentExpirationAlertConfigurationReader reader;

    @Override
    protected PluginPasswordImminentExpirationAlertConfigurationReader getReader() {
        return reader;
    }

}
