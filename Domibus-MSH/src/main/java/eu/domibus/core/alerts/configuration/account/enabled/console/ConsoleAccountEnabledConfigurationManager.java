package eu.domibus.core.alerts.configuration.account.enabled.console;

import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account enabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleAccountEnabledConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConsoleAccountEnabledConfigurationReader reader;

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
