package eu.domibus.core.alerts.configuration.account.disabled.plugin;

import eu.domibus.core.alerts.configuration.AbstractAlertConfigurationManagerWithReader;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of plugin user account disabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginAccountDisabledConfigurationManager
        extends AbstractAlertConfigurationManagerWithReader<AccountDisabledModuleConfiguration, PluginAccountDisabledConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private PluginAccountDisabledConfigurationReader reader;

    @Override
    protected PluginAccountDisabledConfigurationReader getReader() {
        return reader;
    }

}
