
package eu.domibus.plugin;

import eu.domibus.common.NotificationType;

import javax.jms.Queue;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public interface NotificationListener {

    String getBackendName();

    Queue getBackendNotificationQueue();

    BackendConnector.Mode getMode();

    List<NotificationType> getRequiredNotificationTypeList();

    /**
     * Custom action to be performed by the plugins when a message is being deleted
     *
     * @param messageId The message id
     */
    default void deleteMessageCallback(String messageId) {
    }

    /**
     * Notify plugin for various events related to message lifecycle. For more details see {@link NotificationType}
     *
     * @param messageId The message id
     * @param notificationType Notification type
     * @param properties Properties map specific to each notification type
     */
    default void notify(final String messageId, final NotificationType notificationType, final Map<String, Object> properties) {
    }
}
