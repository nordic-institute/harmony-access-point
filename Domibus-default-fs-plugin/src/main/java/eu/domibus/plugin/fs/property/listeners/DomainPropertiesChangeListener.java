package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.worker.FSDomainService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.*;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of the fsplugin domain order and pattern properties.
 */
@Service
public class DomainPropertiesChangeListener implements PluginPropertyChangeListener {

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected FSDomainService fsDomainService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, PROPERTY_PREFIX + ORDER, PROPERTY_PREFIX + EXPRESSION);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        if (StringUtils.endsWithIgnoreCase(propertyName, ORDER)) {
            fsPluginProperties.resetDomains();
        } else if (StringUtils.endsWithIgnoreCase(propertyName, EXPRESSION)) {
            fsDomainService.resetPatterns();
        }
    }

}
