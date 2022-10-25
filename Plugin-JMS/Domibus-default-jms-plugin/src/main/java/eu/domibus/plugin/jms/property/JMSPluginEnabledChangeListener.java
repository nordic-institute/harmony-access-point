package eu.domibus.plugin.jms.property;

import eu.domibus.plugin.jms.JMSPluginImpl;
import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.jms.JMSMessageConstants.JMSPLUGIN_DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles enabling/disabling of jms-plugin for the current domain.
 */
@Component
public class JMSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public JMSPluginEnabledChangeListener(JMSPluginImpl plugin) {
        super(plugin);
    }

    @Override
    protected CharSequence getEnabledPropertyName() {
        return JMSPLUGIN_DOMAIN_ENABLED;
    }

}
