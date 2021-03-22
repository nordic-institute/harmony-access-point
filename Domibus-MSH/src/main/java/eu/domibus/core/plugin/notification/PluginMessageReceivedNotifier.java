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
    public void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, String> properties) {
        final String finalRecipient = (String) properties.get(MessageConstants.FINAL_RECIPIENT);
        DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent(messageId, finalRecipient);
        backendConnectorDelegate.deliverMessage(backendConnector, deliverMessageEvent);
    }
}
