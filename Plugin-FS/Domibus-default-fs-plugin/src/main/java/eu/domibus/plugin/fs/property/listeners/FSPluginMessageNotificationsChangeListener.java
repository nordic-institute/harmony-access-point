package eu.domibus.plugin.fs.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSPluginImpl;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.fs.FSPluginImpl.PLUGIN_NAME;
import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.MESSAGE_NOTIFICATIONS;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 */
@Service
public class FSPluginMessageNotificationsChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginMessageNotificationsChangeListener.class);

    private final FSPluginImpl fsPlugin;

    public FSPluginMessageNotificationsChangeListener(@Qualifier(PLUGIN_NAME) FSPluginImpl fsPlugin) {
        this.fsPlugin = fsPlugin;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, MESSAGE_NOTIFICATIONS);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        fsPlugin.setRequiredNotifications();
    }
}
