package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of the fsplugin domain order property.
 */
@Service
public class DomainOrderPropertyChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, FSPluginProperties.ORDER);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        fsPluginProperties.resetDomains();
    }

}
