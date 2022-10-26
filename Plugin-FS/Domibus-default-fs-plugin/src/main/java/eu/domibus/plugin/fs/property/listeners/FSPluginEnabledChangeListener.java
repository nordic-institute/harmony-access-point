package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.fs.FSPluginImpl.PLUGIN_NAME;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles enabling/disabling of fs-plugin for the current domain.
 */
@Component
public class FSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public FSPluginEnabledChangeListener(BackendConnectorProviderExtService backendConnectorProviderExtService) {
        super(backendConnectorProviderExtService);
    }

    @Override
    protected String getEnabledPropertyName() {
        return DOMAIN_ENABLED;
    }

    @Override
    protected String getName() {
        return PLUGIN_NAME;
    }

}
