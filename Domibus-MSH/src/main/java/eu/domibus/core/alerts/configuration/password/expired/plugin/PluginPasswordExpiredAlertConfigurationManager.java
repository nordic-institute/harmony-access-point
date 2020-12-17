package eu.domibus.core.alerts.configuration.password.expired.plugin;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of plugin user password expired alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginPasswordExpiredAlertConfigurationManager
        extends ReaderAlertConfigurationManager<PasswordExpirationAlertModuleConfiguration, PluginPasswordExpiredAlertConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private PluginPasswordExpiredAlertConfigurationReader reader;

    @Override
    protected PluginPasswordExpiredAlertConfigurationReader getReader() {
        return reader;
    }

}
