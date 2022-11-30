package eu.domibus.core.alert;

import eu.domibus.AbstractIT;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.core.alerts.service.EventServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class AlertEventsTestIT extends AbstractIT {

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    AlertDispatcherService alertDispatcherService;

    static Alert createdAlert;

    @Configuration
    static class ContextConfiguration {

        @Primary
        @Bean
        public AlertDispatcherService alertDispatcherService() {
            return alert -> createdAlert = alert;
        }

    }

    @Test
    public void sendEventStartDateStopped() throws InterruptedException {
        EventType eventType = EventType.ARCHIVING_START_DATE_STOPPED;
        eventService.enqueueEvent(eventType, eventType.name(), new EventProperties());

        Thread.sleep(1000);
        Assert.assertEquals(createdAlert.getAlertType(), AlertType.ARCHIVING_START_DATE_STOPPED);
        Assert.assertEquals(createdAlert.getEvents().size(), 1);
        Assert.assertEquals(createdAlert.getEvents().toArray(new Event[0])[0].getType(), eventType);
    }
}
