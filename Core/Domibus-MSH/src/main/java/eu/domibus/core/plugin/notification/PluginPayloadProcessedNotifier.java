package eu.domibus.core.plugin.notification;

import eu.domibus.common.NotificationType;
import eu.domibus.common.PayloadProcessedEvent;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

/**
 * @author Razvan Cretu
 * @since 5.1
 */
@Service
public class PluginPayloadProcessedNotifier implements PluginEventNotifier<PayloadProcessedEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginPayloadProcessedNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.PAYLOAD_PROCESSED == notificationType;
    }

    @Override
    public void notifyPlugin(PayloadProcessedEvent messageEvent, BackendConnector<?, ?> backendConnector) {
        backendConnectorDelegate.payloadProcessed(backendConnector, messageEvent);
    }
}
