package eu.domibus.core.alerts.configuration.account.enabled.plugin;

import eu.domibus.core.alerts.configuration.AbstractAlertConfigurationManagerWithReader;
import eu.domibus.core.alerts.configuration.AlertConfigurationManager;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the reading of plugin user account enabled alert configuration
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class PluginAccountEnabledConfigurationManager
        extends AbstractAlertConfigurationManagerWithReader<AlertModuleConfigurationBase, PluginAccountEnabledConfigurationReader>
        implements AlertConfigurationManager {

    @Autowired
    private PluginAccountEnabledConfigurationReader reader;

    @Override
    protected PluginAccountEnabledConfigurationReader getReader() {
        return reader;
    }

}
