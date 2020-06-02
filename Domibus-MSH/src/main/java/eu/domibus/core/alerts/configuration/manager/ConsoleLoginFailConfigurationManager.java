package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.ConsoleLoginFailConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class ConsoleLoginFailConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.USER_ACCOUNT_DISABLED;
    }

    @Override
    public LoginFailureModuleConfiguration getConfiguration() {
        return loader.getConfiguration(new ConsoleLoginFailConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
