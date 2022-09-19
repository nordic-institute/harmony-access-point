package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageResponseSentEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0.1
 */
@Service
public class PluginMessageResponseSentNotifier implements PluginEventNotifier {
    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageResponseSentNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_RESPONSE_SENT == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        MessageResponseSentEvent deliverMessageEvent = new MessageResponseSentEvent(messageEntityId, messageId, properties);
        backendConnectorDelegate.messageResponseSent(backendConnector, deliverMessageEvent);
    }
}
