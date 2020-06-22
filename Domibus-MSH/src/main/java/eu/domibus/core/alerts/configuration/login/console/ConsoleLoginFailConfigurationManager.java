package eu.domibus.core.alerts.configuration.login.console;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user login fail alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleLoginFailConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConsoleLoginFailConfigurationReader reader;

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return reader.getAlertType();
    }

    @Override
    public LoginFailureModuleConfiguration getConfiguration() {
        return loader.getConfiguration(reader::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
