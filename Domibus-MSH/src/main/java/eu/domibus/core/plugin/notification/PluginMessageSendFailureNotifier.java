package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageSendFailedEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    public void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, Object> properties) {
        MessageSendFailedEvent messageSendFailedEvent = new MessageSendFailedEvent(messageId);
        backendConnectorDelegate.messageSendFailed(backendConnector, messageSendFailedEvent);
    }
}
