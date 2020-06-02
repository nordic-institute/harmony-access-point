package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.PluginAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginAccountDisabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_DISABLED;
    }

    @Override
    public AccountDisabledModuleConfiguration getConfiguration() {
        return loader.getConfiguration(new PluginAccountDisabledConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
