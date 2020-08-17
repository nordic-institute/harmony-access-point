
package eu.domibus.plugin;

import eu.domibus.common.MessageDeletedEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.plugin.notification.AsyncNotificationListener;

import java.util.List;
import java.util.Map;

/**
 * Domibus sends notifications to the plugins when different events occur. For the list of events see {@link NotificationType}.
 * The plugin can be notified via the backend notification queue or directly via callbacks.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @deprecated Use {@link AsyncNotificationListener}
 */
@Deprecated
public interface NotificationListener extends AsyncNotificationListener {

    /**
     * Get the plugin name
     *
     * @return the plugin name
     * @deprecated Use {@link AsyncNotificationListener#getBackendConnector()}
     */
    @Deprecated
    String getBackendName();

    /**
     * Get the plugin mode. See also {@link eu.domibus.plugin.BackendConnector.Mode}
     *
     * @return the plugin mode
     * @deprecated Use eu.domibus.plugin.BackendConnector#getMode()
     */
    @Deprecated
    BackendConnector.Mode getMode();

    /**
     * Configured notifications sent to the plugin, depending on their MODE (PULL or PUSH)
     *
     * @return the plugin notifications
     * @deprecated Use {@link BackendConnector#getRequiredNotificationTypeList()}
     */
    @Deprecated
    List<NotificationType> getRequiredNotificationTypeList();

    /**
     * Custom action to be performed by the plugins when a message is being deleted
     *
     * @param messageId The message id
     * @deprecated Use {@link BackendConnector#messageDeletedEvent(MessageDeletedEvent)}
     */
    @Deprecated
    default void deleteMessageCallback(String messageId) {
    }

    /**
     * Notify plugin for various events related to message lifecycle. For more details see {@link NotificationType}
     *
     * @param messageId        The message id
     * @param notificationType Notification type
     * @param properties       Properties map specific to each notification type
     * @deprecated This method should not be used anymore. Domibus notifies directly the connector using the lifecycle methods eg messageSendSuccess, messageSendFailed, etc
     */
    @Deprecated
    default void notify(final String messageId, final NotificationType notificationType, final Map<String, Object> properties) {
    }
}
