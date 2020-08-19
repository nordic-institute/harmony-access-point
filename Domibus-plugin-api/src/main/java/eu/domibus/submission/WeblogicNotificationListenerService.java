
package eu.domibus.submission;

import eu.domibus.common.NotificationType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.PluginAsyncNotificationConfiguration;
import eu.domibus.plugin.NotificationListenerService;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.List;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @deprecated use {@link PluginAsyncNotificationConfiguration} and set the JNDI name as the queue name
 */
public class WeblogicNotificationListenerService extends NotificationListenerService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WeblogicNotificationListenerService.class);

    private String queueJndi;

    public WeblogicNotificationListenerService(final Queue queue, final BackendConnector.Mode mode) {
        super(queue, mode);
    }

    public WeblogicNotificationListenerService(final Queue queue, final BackendConnector.Mode mode, final List<NotificationType> requiredNotifications) {
        super(queue, mode, requiredNotifications);
    }

    @Override
    public String getQueueName() throws JMSException {
        return queueJndi;
    }

    public String getQueueJndi() {
        return queueJndi;
    }

    public void setQueueJndi(String queueJndi) {
        this.queueJndi = queueJndi;
    }
}
