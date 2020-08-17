package eu.domibus.plugin;

import eu.domibus.plugin.notification.AsyncNotificationListener;

import javax.jms.Queue;

/**
 * Class responsible for holding the configuration for an {@link AsyncNotificationListener}
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class JMSAsyncNotificationListener implements AsyncNotificationListener {

    protected BackendConnector backendConnector;
    protected Queue queue;
    protected String queueName;

    public JMSAsyncNotificationListener(BackendConnector backendConnector, Queue queue) {
        this.backendConnector = backendConnector;
        this.queue = queue;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public BackendConnector getBackendConnector() {
        return backendConnector;
    }

    @Override
    public Queue getBackendNotificationQueue() {
        return queue;
    }


}
