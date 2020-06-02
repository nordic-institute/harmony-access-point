package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.PluginAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class PluginAccountEnabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<AlertModuleConfigurationBase> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.PLUGIN_USER_ACCOUNT_ENABLED;
    }

    @Override
    public AlertModuleConfigurationBase getConfiguration() {
        return loader.getConfiguration(new PluginAccountEnabledConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
