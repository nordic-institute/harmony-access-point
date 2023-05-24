package eu.domibus.plugin.notification;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * Class responsible for holding the configuration for an {@link AsyncNotificationConfiguration}
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class PluginAsyncNotificationConfiguration implements AsyncNotificationConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginAsyncNotificationConfiguration.class);

    protected BackendConnector backendConnector;
    protected Queue queue;
    protected String queueName;

    public PluginAsyncNotificationConfiguration(BackendConnector backendConnector, Queue queue) {
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
