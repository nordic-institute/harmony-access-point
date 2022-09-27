package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.plugin.BackendConnector;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public interface PluginEventNotifier <T extends MessageEvent> {

    boolean canHandle(NotificationType notificationType);

    void notifyPlugin(T messageEvent, BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties);
}
