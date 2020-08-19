package eu.domibus.plugin;

import eu.domibus.ext.exceptions.JmsExtException;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageNotFoundException;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.Collection;

/**
 * JMS Queue {@link MessageLister} implementation. An instance should be created for each plugin of type PULL which uses a {@link MessageLister} to list messages from a JMS queue.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class QueueMessageLister implements MessageLister {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(QueueMessageLister.class);

    protected JMSExtService jmsExtService;

    //injected by each plugin
    protected Queue backendNotificationQueue;
    protected String backendName;

    public QueueMessageLister(JMSExtService jmsExtService, Queue backendNotificationQueue, String backendName) {
        this.jmsExtService = jmsExtService;
        this.backendNotificationQueue = backendNotificationQueue;
        this.backendName = backendName;
    }

    @Override
    public Collection<String> listPendingMessages() {
        String queueName;
        try {
            queueName = backendNotificationQueue.getQueueName();
        } catch (JMSException jmsEx) {
            throw new JmsExtException("Could not get the queue name for notification listener [" + backendName + "]", jmsEx);
        }
        return jmsExtService.listPendingMessages(queueName);
    }

    @Override
    public void removeFromPending(final String messageId) throws MessageNotFoundException {
        if (backendNotificationQueue == null) {
            LOG.debug("No notification queue configured for plugin [{}]. No message is needed to be deleted", backendName);
            return;
        }
        String queueName;
        try {
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
