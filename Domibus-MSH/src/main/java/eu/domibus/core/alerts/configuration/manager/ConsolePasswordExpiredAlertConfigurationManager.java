package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.ConsolePasswordExpiredAlertConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.configuration.model.RepetitiveAlertModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account password expired alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsolePasswordExpiredAlertConfigurationManager implements AlertConfigurationManager {

    @Autowired
    private ConsolePasswordExpiredAlertConfigurationReader reader;

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
