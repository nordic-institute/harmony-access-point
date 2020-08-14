
package eu.domibus.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.exceptions.JmsExtException;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for holding the configuration for a NotificationListener.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class NotificationListenerService implements NotificationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(NotificationListenerService.class);

    /**
     * Field injection used so to that the user is not obliged to inject it manually
     */
    @Autowired
    protected JMSExtService jmsExtService;

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
        if (getMode() == BackendConnector.Mode.PUSH) {
            LOG.debug("Plugin [{}] type is PUSH. No message is needed to be deleted", getBackendName());
            return;
        }
        if (getBackendNotificationQueue() == null) {
            LOG.debug("No notification queue configured for plugin [{}]. No message is needed to be deleted", getBackendName());
            return;
        }
        String queueName;
        try {
            Queue backendNotificationQueue = getBackendNotificationQueue();
            queueName = backendNotificationQueue.getQueueName();
        } catch (JMSException jmsEx) {
            LOG.error("Error trying to get the queue name", jmsEx);
            throw new JmsExtException("Could not get the queue name", jmsEx);
        }
        try {
            jmsExtService.removeFromPending(queueName, messageId);
            LOG.businessInfo(DomibusMessageCode.BUS_MSG_CONSUMED, messageId, queueName);
        } catch (MessageNotFoundException e) {
            LOG.warn("Could not remove message id [{}] from pending: no message found", messageId);
        }
    }
}
