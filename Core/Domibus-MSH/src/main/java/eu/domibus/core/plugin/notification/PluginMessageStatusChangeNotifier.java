package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageStatusChangeNotifier implements PluginEventNotifier<MessageStatusChangeEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageStatusChangeNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_STATUS_CHANGE == notificationType;
    }

    @Override
    public void notifyPlugin(MessageStatusChangeEvent messageEvent, BackendConnector<?, ?> backendConnector) {
        backendConnectorDelegate.messageStatusChanged(backendConnector, messageEvent);
    }
}
