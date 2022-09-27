package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Service
public class PluginMessageDeletedNotifier implements PluginEventNotifier<MessageDeletedEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageDeletedNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_DELETED == notificationType;
    }

    @Override
    public void notifyPlugin(MessageDeletedEvent messageEvent, BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        backendConnectorDelegate.messageDeletedEvent(backendConnector.getName(), messageEvent);
    }

}
