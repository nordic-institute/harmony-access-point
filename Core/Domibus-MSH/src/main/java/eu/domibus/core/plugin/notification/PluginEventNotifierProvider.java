package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginEventNotifierProvider {

    protected List<PluginEventNotifier> pluginEventNotifierList;

    public PluginEventNotifierProvider(List<PluginEventNotifier> pluginEventNotifierList) {
        this.pluginEventNotifierList = pluginEventNotifierList;
    }

    public PluginEventNotifier getPluginEventNotifier(NotificationType notificationType) {
        for (PluginEventNotifier pluginEventNotifier : pluginEventNotifierList) {
            if (pluginEventNotifier.canHandle(notificationType)) {
                return pluginEventNotifier;
            }
        }
        return null;
    }
}
