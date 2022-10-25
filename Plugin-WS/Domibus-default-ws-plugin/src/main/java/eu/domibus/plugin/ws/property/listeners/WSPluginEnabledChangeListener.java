package eu.domibus.plugin.ws.property.listeners;

import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of ws-plugin for the current domain.
 */
@Component
public class WSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public WSPluginEnabledChangeListener(WSPluginImpl plugin) {
        super(plugin);
    }

    @Override
    protected CharSequence getEnabledPropertyName() {
        return DOMAIN_ENABLED;
    }

}
