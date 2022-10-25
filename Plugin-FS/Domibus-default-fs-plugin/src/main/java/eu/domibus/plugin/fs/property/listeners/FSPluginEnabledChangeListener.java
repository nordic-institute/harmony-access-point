package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.plugin.fs.FSPluginImpl;
import eu.domibus.plugin.property.DefaultEnabledChangeListener;
import org.springframework.stereotype.Component;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.DOMAIN_ENABLED;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Handles enabling/disabling of fs-plugin for the current domain.
 */
@Component
public class FSPluginEnabledChangeListener extends DefaultEnabledChangeListener {

    public FSPluginEnabledChangeListener(FSPluginImpl plugin) {
        super(plugin);
    }

    @Override
    protected CharSequence getEnabledPropertyName() {
        return DOMAIN_ENABLED;
    }

}
