package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageDeletedBatchEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Service
public class PluginMessageDeleteBatchNotifier implements PluginEventNotifier<MessageDeletedBatchEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageDeleteBatchNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_DELETE_BATCH == notificationType;
    }

    @Override
    public void notifyPlugin(MessageDeletedBatchEvent messageEvent, BackendConnector<?, ?> backendConnector) {
        backendConnectorDelegate.messageDeletedBatchEvent(backendConnector.getName(), messageEvent);
    }

}
