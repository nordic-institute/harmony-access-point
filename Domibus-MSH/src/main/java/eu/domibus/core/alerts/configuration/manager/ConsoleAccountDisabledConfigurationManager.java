package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.model.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.reader.ConsoleAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsoleAccountDisabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConsoleAccountDisabledConfigurationReader reader;

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return reader.getAlertType();
    }

    @Override
    public AccountDisabledModuleConfiguration getConfiguration() {
        return loader.getConfiguration(reader::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
