package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.plugin.property.PluginPropertyChangeListener;

/**
 * Adapter class for adapting plugin property change listeners to domibus property change listeners
 * Used in domibus property change notifier class to call polimorphically all property change listeners
 *
 * @author Ion Perpegel
 * @since 4.2
 */
public class PluginPropertyChangeListenerAdapter implements DomibusPropertyChangeListener {

    private PluginPropertyChangeListener pluginPropertyChangeListener;

    public PluginPropertyChangeListenerAdapter(PluginPropertyChangeListener pluginPropertyChangeListener) {
        this.pluginPropertyChangeListener = pluginPropertyChangeListener;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return pluginPropertyChangeListener.handlesProperty(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        try {
            pluginPropertyChangeListener.propertyValueChanged(domainCode, propertyName, propertyValue);
        } catch (DomibusPropertyException ex) {
            throw new DomibusPropertyExtException(ex.getMessage(), ex);
        }
    }
}
