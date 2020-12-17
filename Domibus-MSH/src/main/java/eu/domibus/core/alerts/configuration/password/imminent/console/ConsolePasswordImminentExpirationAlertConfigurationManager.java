package eu.domibus.core.alerts.configuration.password.imminent.console;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user account password imminebt expiration alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsolePasswordImminentExpirationAlertConfigurationManager
        extends ReaderAlertConfigurationManager<PasswordExpirationAlertModuleConfiguration, ConsolePasswordImminentExpirationAlertConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private ConsolePasswordImminentExpirationAlertConfigurationReader reader;

    @Override
    protected ConsolePasswordImminentExpirationAlertConfigurationReader getReader() {
        return reader;
    }

}
