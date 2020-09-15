package eu.domibus.core.alerts.configuration.account.enabled.console;

import eu.domibus.core.alerts.configuration.AbstractAlertConfigurationManagerWithReader;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account enabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleAccountEnabledConfigurationManager
        extends AbstractAlertConfigurationManagerWithReader<AlertModuleConfigurationBase, ConsoleAccountEnabledConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private ConsoleAccountEnabledConfigurationReader reader;

    @Override
    protected ConsoleAccountEnabledConfigurationReader getReader() {
        return reader;
    }

}
