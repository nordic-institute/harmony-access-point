package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageEvent;
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
public class PluginMessageSendFailureNotifier implements PluginEventNotifier<MessageSendFailedEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageSendFailureNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_SEND_FAILURE == notificationType;
    }

    @Override
    public void notifyPlugin(MessageSendFailedEvent messageEvent, BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        backendConnectorDelegate.messageSendFailed(backendConnector, messageEvent);
    }
}
