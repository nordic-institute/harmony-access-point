package eu.domibus.core.plugin.notification;

import eu.domibus.common.*;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageReceivedNotifier implements PluginEventNotifier <MessageReceivedEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageReceivedNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_RECEIVED == notificationType;
    }

    @Override
    public void notifyPlugin(MessageReceivedEvent messageEvent, BackendConnector<?, ?> backendConnector) {
        backendConnectorDelegate.deliverMessage(backendConnector, messageEvent);
    }
}
