package eu.domibus.core.alerts.configuration.manager;

import eu.domibus.core.alerts.configuration.reader.ConsoleAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class ConsoleAccountDisabledConfigurationManager implements AlertConfigurationManager {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConsoleAccountDisabledConfigurationManager.class);

    @Autowired
    private ConfigurationLoader<AccountDisabledModuleConfiguration> loader;

    @Override
    public AlertType getAlertType() {
        return AlertType.USER_ACCOUNT_DISABLED;
    }

    @Override
    public AccountDisabledModuleConfiguration getConfiguration() {
        return loader.getConfiguration(new ConsoleAccountDisabledConfigurationReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }
}
