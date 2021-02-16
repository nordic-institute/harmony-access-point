package eu.domibus.core.alerts.service;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PluginEventServiceImplTest {


    @Tested
    private PluginEventServiceImpl eventService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;

    @Test
    public void enqueueMessageEvent() {
        eventService.enqueueMessageEvent(new AlertEvent());
        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.PLUGIN.getQueueSelector());
            times = 1;
            Assert.assertEquals(EventType.PLUGIN, event.getType());
        }};
    }
}