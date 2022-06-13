package eu.domibus.core.alerts.configuration.login.plugin;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of plugin user login fail alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginLoginFailConfigurationManager
        extends ReaderAlertConfigurationManager<LoginFailureModuleConfiguration, PluginLoginFailConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private PluginLoginFailConfigurationReader reader;

    @Override
    protected PluginLoginFailConfigurationReader getReader() {
        return reader;
    }
}
