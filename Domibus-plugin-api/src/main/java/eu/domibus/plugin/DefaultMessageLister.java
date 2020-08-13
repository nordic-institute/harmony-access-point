package eu.domibus.plugin;

import eu.domibus.ext.exceptions.JmsExtException;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import java.util.Collection;

/**
 * Default MessageLister implementation. An instance should be created for each plugin which uses MessageLister.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DefaultMessageLister implements MessageLister {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultMessageLister.class);

    @Autowired
    protected JMSExtService jmsExtService;

    //injected by each plugin
    protected NotificationListenerService notificationListenerService;

    @Override
    public Collection<String> listPendingMessages() {
        if (notificationListenerService.getMode() == BackendConnector.Mode.PUSH) {
            throw new UnsupportedOperationException("listPendingMessages method is only available for clients using Mode.PULL");
        }

        String queueName;
        try {
            queueName = notificationListenerService.getBackendNotificationQueue().getQueueName();
        } catch (JMSException jmsEx) {
            throw new JmsExtException("Could not get the queue name for notification listener [" + notificationListenerService.getBackendName() + "]", jmsEx);
        }
        return jmsExtService.listPendingMessages(queueName);
    }

    @Override
    public void removeFromPending(final String messageId) throws MessageNotFoundException {
        notificationListenerService.deleteMessageCallback(messageId);
    }

    public void setNotificationListenerService(NotificationListenerService notificationListenerService) {
        this.notificationListenerService = notificationListenerService;
    }

    public NotificationListenerService getNotificationListenerService() {
        return notificationListenerService;
    }
}
