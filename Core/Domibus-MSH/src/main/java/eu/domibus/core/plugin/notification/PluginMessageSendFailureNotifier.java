package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageSendFailedEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

import static eu.domibus.messaging.MessageConstants.FINAL_RECIPIENT;
import static eu.domibus.messaging.MessageConstants.ORIGINAL_SENDER;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageSendFailureNotifier implements PluginEventNotifier {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageSendFailureNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_SEND_FAILURE == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        MessageSendFailedEvent messageSendFailedEvent = new MessageSendFailedEvent(messageEntityId, messageId);
        messageSendFailedEvent.addProperty(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT));
        messageSendFailedEvent.addProperty(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER));
        backendConnectorDelegate.messageSendFailed(backendConnector, messageSendFailedEvent);
    }
}
