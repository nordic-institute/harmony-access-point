package eu.domibus.plugin.notification;

import eu.domibus.plugin.BackendConnector;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * Responsible for configuring a connector to receive async notifications via a JMS queue
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public interface AsyncNotificationListener {

    /**
     * The connector which will receive async notifications via the configured JMS queue
     *
     * @return the connector
     */
    BackendConnector getBackendConnector();

    /**
     * Gets the plugin notification queue that will be used by Domibus to notify the plugin asynchronously
     *
     * @return
     */
    Queue getBackendNotificationQueue();

    /**
     * The Queue name or queue jndi name
     *
     * @return the queue name or queue jndi name
     * @throws JMSException in case the queue name cannot be get
     */
    default String getQueueName() throws JMSException {
        return getBackendNotificationQueue().getQueueName();
    }
}
