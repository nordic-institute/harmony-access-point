package eu.domibus.core.alerts.configuration.account.disabled.console;

import eu.domibus.core.alerts.configuration.AbstractAlertConfigurationManagerWithReader;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account disabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleAccountDisabledConfigurationManager
        extends AbstractAlertConfigurationManagerWithReader<AccountDisabledModuleConfiguration, ConsoleAccountDisabledConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private ConsoleAccountDisabledConfigurationReader reader;

    @Override
    protected ConsoleAccountDisabledConfigurationReader getReader() {
        return reader;
    }

}
