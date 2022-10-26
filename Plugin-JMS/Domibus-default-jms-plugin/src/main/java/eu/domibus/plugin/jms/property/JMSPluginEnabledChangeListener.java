package eu.domibus.plugin.jms.property;

import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMSPLUGIN_DOMAIN_ENABLED;
import static eu.domibus.plugin.jms.JMSPluginImpl.PLUGIN_NAME;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of jms-plugin for the current domain.
 */
@Component
public class JMSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public JMSPluginEnabledChangeListener(BackendConnectorProviderExtService backendConnectorProviderExtService) {
        super(backendConnectorProviderExtService);
    }

    @Override
    protected String getEnabledPropertyName() {
        return JMSPLUGIN_DOMAIN_ENABLED;
    }

    @Override
    protected String getName() {
        return PLUGIN_NAME;
    }
}
