package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class extended by alert configuration managers
 * Implements all methods from the AlertConfigurationManager interface by the help of the coresponding Reader and Loader classes
 * Used to avoid duplicating the same code across those managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public abstract class ReaderAlertConfigurationManager<AMC extends AlertModuleConfiguration, ACR extends AlertConfigurationReader<AMC>>
        implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<AMC> loader;

    protected abstract ACR getReader();

    @Override
    public AlertType getAlertType() {
        return getReader().getAlertType();
    }

    @Override
    public AMC getConfiguration() {
        return loader.getConfiguration(getReader()::readConfiguration);
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }

}
