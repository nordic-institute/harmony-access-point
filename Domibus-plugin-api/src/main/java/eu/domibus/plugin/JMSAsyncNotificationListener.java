package eu.domibus.plugin;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * Class responsible for holding the configuration for an {@link AsyncNotificationListener}
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class JMSAsyncNotificationListener implements AsyncNotificationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSAsyncNotificationListener.class);

    protected BackendConnector backendConnector;
    protected Queue queue;
    protected String queueName;

    public JMSAsyncNotificationListener(BackendConnector backendConnector, Queue queue) {
        this.backendConnector = backendConnector;
        this.queue = queue;
    }

    @Override
    public String getQueueName() throws JMSException {
        if (StringUtils.isNoneEmpty(queueName)) {
            LOG.trace("Using custom queue name [{}]", queueName);
            return queueName;
        }
        return queue.getQueueName();
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
