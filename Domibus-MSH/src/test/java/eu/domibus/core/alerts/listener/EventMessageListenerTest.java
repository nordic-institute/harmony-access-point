package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.util.DatabaseUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class EventMessageListenerTest {

    @Tested
    private EventMessageListener eventMessageListener;

    @Injectable
    private EventService eventService;

    @Injectable
    private AlertService alertService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DatabaseUtil databaseUtil;

    @Test
    public void onMessageEvent(@Mocked final Event event, @Mocked final Alert alert) {
        final String domain = "domain";
        new Expectations(){{
            alertService.createAlertOnEvent(event);
            result=alert;
        }};
        eventMessageListener.onMessageEvent(event, domain);
        new Verifications(){{
            domainContextProvider.setCurrentDomain(domain);
            eventService.enrichMessageEvent(event);
            eventService.persistEvent(event);
            alertService.enqueueAlert(alert);
        }};


    }
}