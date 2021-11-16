package eu.domibus.core.plugin.notification;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageReceivedNotifier implements PluginEventNotifier {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageReceivedNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_RECEIVED == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector<?, ?> backendConnector, long messageEntityId, String messageId, Map<String, String> properties) {
        DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent(messageEntityId, messageId, properties);
        backendConnectorDelegate.deliverMessage(backendConnector, deliverMessageEvent);
    }
}
