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
import org.apache.commons.lang3.StringUtils;
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
    public static final String EMAIL_BODY_300_LONG = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789" +
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
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
            result = EMAIL_BODY_300_LONG;

            alertEvent.getName();
            result = "AlertName";

            alertEvent.getEmailSubject();
            result = "EmailSubject";
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
            Assert.assertEquals(StringUtils.substring(EMAIL_BODY_300_LONG, 0, 255), event.getProperties().get(AlertServiceImpl.ALERT_DESCRIPTION).toString());
            Assert.assertEquals(StringUtils.substring(EMAIL_BODY_300_LONG, 255), event.getProperties().get(AlertServiceImpl.ALERT_DESCRIPTION + "_1").toString());
        }};
    }
}