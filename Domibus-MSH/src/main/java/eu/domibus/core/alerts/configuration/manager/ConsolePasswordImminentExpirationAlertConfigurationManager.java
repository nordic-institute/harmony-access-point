package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.model.RepetitiveAlertModuleConfiguration;
import eu.domibus.core.alerts.configuration.reader.ConsolePasswordImminentExpirationAlertConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsolePasswordImminentExpirationAlertConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConsolePasswordImminentExpirationAlertConfigurationReader reader;

    @Autowired
    private ConfigurationLoader<RepetitiveAlertModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return reader.getAlertType();
    }

    @Override
    public RepetitiveAlertModuleConfiguration getConfiguration() {
        return loader.getConfiguration(reader::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
