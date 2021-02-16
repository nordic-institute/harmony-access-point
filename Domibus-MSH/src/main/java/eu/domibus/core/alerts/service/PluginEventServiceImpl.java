package eu.domibus.core.alerts.service;

import eu.domibus.api.alerts.PluginEventService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.Map;

/**
 * {@inheritDoc}
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
public class PluginEventServiceImpl implements PluginEventService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginEventServiceImpl.class);
    private static final String PLUGIN_EVENT_ADDED_TO_THE_QUEUE = "Plugin Event:[{}] added to the queue";
    private final JMSManager jmsManager;

    private final Queue alertMessageQueue;

    public PluginEventServiceImpl(JMSManager jmsManager, @Qualifier("alertMessageQueue") Queue alertMessageQueue) {
        this.jmsManager = jmsManager;
        this.alertMessageQueue = alertMessageQueue;
    }

    public void enqueueMessageEvent(Map<String, String> properties) {
        Event event = new Event(EventType.PLUGIN_DEFAULT);
        for (Map.Entry<String, String> stringStringEntry : properties.entrySet()) {
            event.addStringKeyValue(stringStringEntry.getKey(), stringStringEntry.getValue());
        }

        jmsManager.convertAndSendToQueue(event, alertMessageQueue, EventType.PLUGIN_DEFAULT.getQueueSelector());
        LOG.debug(PLUGIN_EVENT_ADDED_TO_THE_QUEUE, event);
    }

}
