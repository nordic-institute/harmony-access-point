package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.PluginLoginFailConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginLoginFailConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<LoginFailureModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_DISABLED;
    }

    @Override
    public LoginFailureModuleConfiguration getConfiguration() {
        return loader.getConfiguration(new PluginLoginFailConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
