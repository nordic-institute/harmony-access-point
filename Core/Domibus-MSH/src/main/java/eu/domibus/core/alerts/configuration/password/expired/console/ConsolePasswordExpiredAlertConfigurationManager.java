package eu.domibus.core.alerts.configuration.password.expired.console;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account password expired alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsolePasswordExpiredAlertConfigurationManager
        extends ReaderAlertConfigurationManager<PasswordExpirationAlertModuleConfiguration, ConsolePasswordExpiredAlertConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private ConsolePasswordExpiredAlertConfigurationReader reader;

    @Override
    protected ConsolePasswordExpiredAlertConfigurationReader getReader() {
        return reader;
    }

}
