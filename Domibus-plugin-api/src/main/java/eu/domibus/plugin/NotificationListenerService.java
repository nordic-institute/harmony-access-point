
package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class responsible for holding the configuration for a NotificationListener.
 *
 * @author Cosmin Baciu
 * @since 4.2
 * @deprecated Use {@link PluginAsyncNotificationConfiguration}
 */
@Deprecated
public class NotificationListenerService implements NotificationListener, MessageLister {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NotificationListenerService.class);

    @Autowired
    protected ObjectProvider<QueueMessageLister> queueMessageListerObjectProvider;

    protected QueueMessageLister queueMessageLister;

    //the following fields are provided by the plugin
    protected Queue backendNotificationQueue;
    /**
     * Used to override the queue name. For WebLogic and WildFly, set the queue JNDI name as the queue name
     */
    protected String queueName;
    protected BackendConnector.Mode mode;
    protected BackendConnector backendConnector;
    protected List<NotificationType> requiredNotifications;

    public NotificationListenerService(final Queue queue, final BackendConnector.Mode mode) {
        this.backendNotificationQueue = queue;
        this.mode = mode;

        requiredNotifications = new ArrayList<>();
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED);
        requiredNotifications.add(NotificationType.MESSAGE_SEND_FAILURE);
        requiredNotifications.add(NotificationType.MESSAGE_RECEIVED_FAILURE);

        if (BackendConnector.Mode.PUSH.equals(mode)) {
            requiredNotifications.add(NotificationType.MESSAGE_SEND_SUCCESS);
            requiredNotifications.add(NotificationType.MESSAGE_STATUS_CHANGE);
        }
    }

    public NotificationListenerService(final Queue queue, final BackendConnector.Mode mode, final List<NotificationType> requiredNotifications) {
        this.backendNotificationQueue = queue;
        this.mode = mode;
        this.requiredNotifications = requiredNotifications;
    }

    @PostConstruct
    public void init() {
        if (mode == BackendConnector.Mode.PUSH) {
            LOG.debug("Plugin [{}] type is PUSH. No queue message  lister is needed", getBackendName());
            return;
        }

        queueMessageLister = queueMessageListerObjectProvider.getObject(backendNotificationQueue, getBackendName());
    }

    public void setBackendConnector(final BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    public BackendConnector getBackendConnector() {
        return backendConnector;
    }

    public List<NotificationType> getRequiredNotifications() {
        return requiredNotifications;
    }

    @Override
    public String getBackendName() {
        return backendConnector.getName();
    }

    @Override
    public Queue getBackendNotificationQueue() {
        return backendNotificationQueue;
    }

    @Override
    public String getQueueName() throws JMSException {
        if (StringUtils.isNoneEmpty(queueName)) {
            LOG.trace("Using custom queue name [{}]", queueName);
            return queueName;
        }
        return backendNotificationQueue.getQueueName();
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public BackendConnector.Mode getMode() {
        return this.mode;
    }

    @Override
    public List<NotificationType> getRequiredNotificationTypeList() {
        return this.requiredNotifications;
    }

    @Override
    public void deleteMessageCallback(String messageId) {
        try {
            removeFromPending(messageId);
        } catch (MessageNotFoundException e) {
            LOG.debug("No message with id [{}] to remove from the pending list", messageId, e);
        }
    }

    @Override
    public Collection<String> listPendingMessages() {
        if (mode == BackendConnector.Mode.PUSH) {
            throw new UnsupportedOperationException("listPendingMessages method is only available for clients using Mode.PULL");
        }
        return queueMessageLister.listPendingMessages();
    }

    @Override
    public void removeFromPending(String messageId) throws MessageNotFoundException {
        if (mode == BackendConnector.Mode.PUSH) {
            LOG.debug("Plugin [{}] type is PUSH. No message is needed to be deleted", getBackendName());
            return;
        }
        queueMessageLister.removeFromPending(messageId);
    }
}
