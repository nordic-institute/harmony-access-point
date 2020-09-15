package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class extended by alert configuration managers
 * Implements most methods from the AlertConfigurationManager interface by the help of the Loader class and read0configuration method
 * Used to avoid duplicating the same code across those managers
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public abstract class AbstractAlertConfigurationManagerWithReaderMethod<AMC extends AlertModuleConfiguration>
        implements AlertConfigurationManager {

    @Autowired
    private ConfigurationLoader<AMC> loader;

    protected abstract ConfigurationReader<AMC> getReaderMethod();

    @Override
    public AMC getConfiguration() {
        return loader.getConfiguration(getReaderMethod());
    }

    @Override
    public void reset() {
        loader.resetConfiguration();
    }

}
