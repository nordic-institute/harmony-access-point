package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.PluginAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.configuration.model.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginAccountEnabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private PluginAccountEnabledConfigurationReader reader;

    @Autowired
    private ConfigurationLoader<AlertModuleConfigurationBase> loader;

    @Override
    public AlertType getAlertType() {
        return reader.getAlertType();
    }

    @Override
    public AlertModuleConfigurationBase getConfiguration() {
        return loader.getConfiguration(reader::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
