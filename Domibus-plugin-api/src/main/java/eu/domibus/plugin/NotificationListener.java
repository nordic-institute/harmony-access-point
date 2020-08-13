
package eu.domibus.plugin;

import eu.domibus.common.NotificationType;

import javax.jms.Queue;
import java.util.List;
import java.util.Map;

/**
 * Domibus sends notifications to the plugins when different events occur. For the list of events see {@link NotificationType}.
 * The plugin can be notified via the backend notification queue or directly via callbacks.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public interface NotificationListener {

    /**
     * Get the plugin name
     *
     * @return the plugin name
     */
    String getBackendName();

    /**
     * Gets the plugin notification queue that will be used by Domibus to notify the plugin
     *
     * @return
     */
    Queue getBackendNotificationQueue();

    /**
     * Get the plugin mode. See also {@link eu.domibus.plugin.BackendConnector.Mode}
     *
     * @return the plugin mode
     */
    BackendConnector.Mode getMode();

    /**
     * Configured notifications sent to the plugin, depending on their MODE (PULL or PUSH)
     *
     * @return the plugin notifications
     */
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
     * @deprecated This method should not be used anymore. Domibus notifies directly the connector using the lifecycle methods eg messageSendSuccess, messageSendFailed, etc
     */
    @Deprecated
    default void notify(final String messageId, final NotificationType notificationType, final Map<String, Object> properties) {
    }
}
