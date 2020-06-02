package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.ConsoleAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.configuration.reader.ConsoleAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsoleAccountEnabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<AlertModuleConfigurationBase> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.USER_ACCOUNT_ENABLED;
    }

    @Override
    public AlertModuleConfigurationBase getConfiguration() {
        return loader.getConfiguration(new ConsoleAccountEnabledConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
