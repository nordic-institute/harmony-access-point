package eu.domibus.core.plugin.notification;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.MessageReceiveReplySentEvent;
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
public class PluginMessageReceivedReplySentNotifier implements PluginEventNotifier {
    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageReceivedReplySentNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_RECEIVED_REPLY_SENT == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        MessageReceiveReplySentEvent deliverMessageEvent = new MessageReceiveReplySentEvent(messageEntityId, messageId, properties);
        backendConnectorDelegate.messageReceiveReplySent(backendConnector, deliverMessageEvent);
    }
}
