package eu.domibus.core.alert;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AbstractPropertyValue;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.core.alerts.service.EventServiceImpl;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.messaging.XmlProcessingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class AlertEventsTestIT extends AbstractIT {

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    UserDao userDao;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    static List<Alert> dispatchedAlerts = new ArrayList<>();

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public AlertDispatcherService alertDispatcherService() {
            return alert -> dispatchedAlerts.add(alert);
        }

    }

    @Before
    public void setUp() throws XmlProcessingException, IOException {
        dispatchedAlerts.clear();
        uploadPmode(SERVICE_PORT);
    }

    @Test
    public void sendEventStartDateStopped() throws InterruptedException {
        EventType eventType = EventType.ARCHIVING_START_DATE_STOPPED;
        eventService.enqueueEvent(eventType, eventType.name(), new EventProperties());

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.ARCHIVING_START_DATE_STOPPED);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Assert.assertEquals(alert.getEvents().toArray(new Event[0])[0].getType(), eventType);
    }

    @Test
    public void sendEventMessageNotFinal() throws InterruptedException {
        String messageId = "messageId";
        MessageStatus messageStatus = MessageStatus.RECEIVED;

        eventService.enqueueEvent(EventType.ARCHIVING_MESSAGES_NON_FINAL, messageId, new EventProperties(messageId, messageStatus.name()));

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.ARCHIVING_MESSAGES_NON_FINAL);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Event event = alert.getEvents().toArray(new Event[0])[0];
        Assert.assertEquals(event.getType(), EventType.ARCHIVING_MESSAGES_NON_FINAL);
        Map<String, AbstractPropertyValue> properties = event.getProperties();
        Assert.assertEquals(properties.size(), 3);
        Assert.assertEquals(properties.get("OLD_STATUS").getValue(), messageStatus.name());
        Assert.assertEquals(properties.get("MESSAGE_ID").getValue(), messageId);
        Assert.assertEquals(properties.get("EVENT_IDENTIFIER").getValue(), messageId);
    }

    @Test
    public void sendMessagingEvent() throws InterruptedException {
        String messageId = this.getClass().getName() + "msg-test-1";
        UserMessageLog uml = messageDaoTestUtil.createTestMessage(messageId);
        eventService.enqueueMessageStatusChangedEvent(messageId, MessageStatus.SEND_ENQUEUED, MessageStatus.SEND_FAILURE, MSHRole.SENDING);

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.MSG_STATUS_CHANGED);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Event event = alert.getEvents().toArray(new Event[0])[0];
        Assert.assertEquals(event.getType(), EventType.MSG_STATUS_CHANGED);
        Map<String, AbstractPropertyValue> properties = event.getProperties();
        Assert.assertEquals(properties.size(), 4);
        Assert.assertEquals(properties.get("OLD_STATUS").getValue(), MessageStatus.SEND_ENQUEUED.name());
        Assert.assertEquals(properties.get("NEW_STATUS").getValue(), MessageStatus.SEND_FAILURE.name());
        Assert.assertEquals(properties.get("MESSAGE_ID").getValue(), messageId);

        messageDaoTestUtil.deleteMessages(Arrays.asList(uml.getEntityId()));
    }

    @Test
    public void sendMessagingEventStatusWithoutAlert() throws InterruptedException {
        String messageId = this.getClass().getName() + "msg-test-2";
        UserMessageLog uml = messageDaoTestUtil.createTestMessage(messageId);
        eventService.enqueueMessageStatusChangedEvent(messageId, MessageStatus.SEND_ENQUEUED, MessageStatus.ACKNOWLEDGED, MSHRole.SENDING);

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 0);

        messageDaoTestUtil.deleteMessages(Arrays.asList(uml.getEntityId()));
    }

    @Test
    public void sendRepetitiveAlert() throws InterruptedException {
        EventType eventType = EventType.PLUGIN_PASSWORD_EXPIRED;
        UserEntityBase user = userDao.listUsers().get(0);
        int maxPasswordAgeInDays = 10;

        eventService.enqueuePasswordExpirationEvent(eventType, user, maxPasswordAgeInDays);

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.PLUGIN_PASSWORD_EXPIRED);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Assert.assertEquals(alert.getEvents().toArray(new Event[0])[0].getType(), eventType);

        eventService.enqueuePasswordExpirationEvent(eventType, user, maxPasswordAgeInDays);
        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);

        eventService.enqueuePasswordExpirationEvent(eventType, user, maxPasswordAgeInDays);
        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
    }

    @Test
    public void sendFrequencyAlert() throws InterruptedException {
        String partitionName = "partitionName";

        eventService.enqueueEvent(EventType.PARTITION_CHECK, partitionName, new EventProperties(partitionName));

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.PARTITION_CHECK);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Assert.assertEquals(alert.getEvents().toArray(new Event[0])[0].getType(), EventType.PARTITION_CHECK);

        eventService.enqueueEvent(EventType.PARTITION_CHECK, partitionName, new EventProperties(partitionName));
        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);

        eventService.enqueueEvent(EventType.PARTITION_CHECK, partitionName, new EventProperties(partitionName));
        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
    }

    @Test
    public void sendConnMonitorAlert() throws InterruptedException {
        MessageStatus oldStatus = MessageStatus.SEND_ENQUEUED;
        MessageStatus newStatus = MessageStatus.SEND_FAILURE;
        String fromParty = "fromParty";
        String toParty = "toParty";
        String messageId = "messageId";

        eventService.enqueueMonitoringEvent(messageId, MSHRole.RECEIVING, oldStatus, newStatus, fromParty, toParty);

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 1);
        Alert alert = dispatchedAlerts.get(0);
        Assert.assertEquals(alert.getAlertType(), AlertType.CONNECTION_MONITORING_FAILED);
        Assert.assertEquals(alert.getEvents().size(), 1);
        Assert.assertEquals(alert.getEvents().toArray(new Event[0])[0].getType(), EventType.CONNECTION_MONITORING_FAILED);
    }

    @Test
    public void sendConnMonitorAlertNoAlert() throws InterruptedException {
        MessageStatus oldStatus = MessageStatus.SEND_ENQUEUED;
        MessageStatus newStatus = MessageStatus.ACKNOWLEDGED;
        String fromParty = "fromParty";
        String toParty = "toParty";
        String messageId = "messageId";

        eventService.enqueueMonitoringEvent(messageId, MSHRole.SENDING, oldStatus, newStatus, fromParty, toParty);

        Thread.sleep(1000);
        Assert.assertEquals(dispatchedAlerts.size(), 0);
    }

}
