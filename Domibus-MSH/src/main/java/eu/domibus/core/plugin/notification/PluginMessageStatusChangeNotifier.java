package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageStatusChangeNotifier implements PluginEventNotifier {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageStatusChangeNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_STATUS_CHANGE == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, String> properties) {
        MessageStatusChangeEvent event = new MessageStatusChangeEvent();
        event.setMessageId(messageId);

        final String fromStatus = properties.get(MessageConstants.STATUS_FROM);
        if (StringUtils.isNotEmpty(fromStatus)) {
            event.setFromStatus(MessageStatus.valueOf(fromStatus));
        }
        event.setToStatus(MessageStatus.valueOf(properties.get(MessageConstants.STATUS_TO)));
        event.setChangeTimestamp(new Timestamp( NumberUtils.toLong(properties.get(MessageConstants.CHANGE_TIMESTAMP)) ));
        event.addProperty("service", properties.get(MessageConstants.SERVICE));
        event.addProperty("serviceType", properties.get(MessageConstants.SERVICE_TYPE));
        event.addProperty("action", properties.get(MessageConstants.ACTION));
        backendConnectorDelegate.messageStatusChanged(backendConnector, event);
    }
}
