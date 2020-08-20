package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import eu.domibus.plugin.BackendConnector;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public interface PluginEventNotifier {

    boolean canHandle(NotificationType notificationType);

    void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, Object> properties);
}
