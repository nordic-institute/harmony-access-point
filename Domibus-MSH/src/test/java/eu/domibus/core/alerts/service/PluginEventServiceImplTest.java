package eu.domibus.core.alerts.service;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.HashMap;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PluginEventServiceImplTest {


    public static final String ALERT_NAME = "AlertName";
    public static final String EMAIL_SUBJECT = "EmailSubject";
    public static final String EMAIL_BODY = "EmailBody";
    @Tested
    private PluginEventServiceImpl eventService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private Queue alertMessageQueue;

    @Test
    public void enqueueMessageEvent_empty(@Injectable AlertEvent alertEvent) {
        new Expectations() {{
            alertEvent.getAlertLevel();
            result = null;
        }};

        eventService.enqueueMessageEvent(alertEvent);

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.PLUGIN.getQueueSelector());
            times = 1;
            Assert.assertEquals(EventType.PLUGIN, event.getType());
        }};
    }

    @Test
    public void enqueueMessageEvent_full(@Injectable AlertEvent alertEvent) {
        Map<String, String> props = new HashMap<>();
        props.put("Test", "Test");
        new Expectations() {{
            alertEvent.getProperties();
            result = props;

            alertEvent.getAlertLevel();
            result = AlertLevel.MEDIUM;

            alertEvent.getEmailBody();
            result = "EmailBody";

            alertEvent.getName();
            result = "AlertName";

            alertEvent.getEmailSubject();
            result = "EmailSubject";

            alertEvent.isActive();
            result = true;
        }};

        eventService.enqueueMessageEvent(alertEvent);

        new Verifications() {{
            Event event;
            jmsManager.convertAndSendToQueue(event = withCapture(), alertMessageQueue, EventType.PLUGIN.getQueueSelector());
            times = 1;
            Assert.assertEquals(EventType.PLUGIN, event.getType());
            Assert.assertEquals(AlertLevel.MEDIUM.name(), event.getProperties().get(AlertServiceImpl.ALERT_LEVEL).toString());
            Assert.assertEquals(ALERT_NAME, event.getProperties().get(AlertServiceImpl.ALERT_NAME).toString());
            Assert.assertEquals(Boolean.TRUE.toString(), event.getProperties().get(AlertServiceImpl.ALERT_ACTIVE).toString());
            Assert.assertEquals(EMAIL_SUBJECT, event.getProperties().get(AlertServiceImpl.ALERT_SUBJECT).toString());
            Assert.assertEquals(EMAIL_BODY, event.getProperties().get(AlertServiceImpl.ALERT_DESCRIPTION).toString());
        }};
    }
}