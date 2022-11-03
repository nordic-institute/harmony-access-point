package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_ALERT;
import static eu.domibus.core.alerts.service.EventServiceImpl.ALERT_JMS_LISTENER_CONTAINER_FACTORY;

/**
 * @author Ion Perpegel
 * @since 5.1
 *
 * The event queue listener for all simple, non-repetitive events
 */
@Component
public class DefaultEventListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultEventListener.class);

    private final EventService eventService;

    private final AlertService alertService;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    public DefaultEventListener(EventService eventService, AlertService alertService, DomainContextProvider domainContextProvider, DatabaseUtil databaseUtil) {
        this.eventService = eventService;
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
    }

    @JmsListener(containerFactory = ALERT_JMS_LISTENER_CONTAINER_FACTORY, destination = "${"+ DOMIBUS_JMS_QUEUE_ALERT + "}",
            selector = "selector = 'DefaultEventListener'")
    public void onEvent(final Event event, @Header(name = "DOMAIN") String domain) {
        saveEventAndTriggerAlert(event, domain);
    }

    private void saveEventAndTriggerAlert(Event event, @Header(name = "DOMAIN") String domain) {
        LOG.debug("Event:[{}] for domain:[{}]", event, domain);
        domainContextProvider.setCurrentDomain(domain);
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
        
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
