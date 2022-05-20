package eu.domibus.core.alerts.configuration.login.console;

import eu.domibus.core.alerts.configuration.ReaderAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of console user login fail alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class ConsoleLoginFailConfigurationManager
        extends ReaderAlertConfigurationManager<LoginFailureModuleConfiguration, ConsoleLoginFailConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private ConsoleLoginFailConfigurationReader reader;

    @Override
    protected ConsoleLoginFailConfigurationReader getReader() {
        return reader;
    }

}
