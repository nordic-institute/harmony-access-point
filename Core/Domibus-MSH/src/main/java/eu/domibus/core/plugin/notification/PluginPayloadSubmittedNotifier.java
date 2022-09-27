package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import eu.domibus.common.PayloadSubmittedEvent;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Service
public class PluginPayloadSubmittedNotifier implements PluginEventNotifier<PayloadSubmittedEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginPayloadSubmittedNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.PAYLOAD_SUBMITTED == notificationType;
    }

    @Override
    public void notifyPlugin(PayloadSubmittedEvent messageEvent, BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        backendConnectorDelegate.payloadSubmitted(backendConnector, messageEvent);
    }
}
