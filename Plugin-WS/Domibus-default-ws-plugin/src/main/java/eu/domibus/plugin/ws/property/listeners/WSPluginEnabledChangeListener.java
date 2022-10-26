package eu.domibus.plugin.ws.property.listeners;

import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.ws.connector.WSPluginImpl.PLUGIN_NAME;
import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of ws-plugin for the current domain.
 */
@Component
public class WSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public WSPluginEnabledChangeListener(BackendConnectorProviderExtService backendConnectorProviderExtService) {
        super(backendConnectorProviderExtService);
    }

    @Override
    protected CharSequence getEnabledPropertyName() {
        return DOMAIN_ENABLED;
    }

    @Override
    protected String getName() {
        return PLUGIN_NAME;
    }
}
