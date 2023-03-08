package eu.domibus.plugin.ws.property.listeners;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static eu.domibus.plugin.ws.property.WSPluginPropertyManager.MESSAGE_NOTIFICATIONS;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 */
@Service
public class WSPluginMessageNotificationsChangeListener implements PluginPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginMessageNotificationsChangeListener.class);

    private final WSPluginImpl wsPlugin;

    public WSPluginMessageNotificationsChangeListener(@Qualifier(WSPluginImpl.PLUGIN_NAME) WSPluginImpl wsPlugin) {
        this.wsPlugin = wsPlugin;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        boolean doesHandle = StringUtils.equals(propertyName, MESSAGE_NOTIFICATIONS);
        LOG.trace("Handling [{}] property: [{}]", propertyName, doesHandle);
        return doesHandle;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        wsPlugin.setRequiredNotifications();
    }
}
