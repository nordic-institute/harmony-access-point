package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.EventServiceImpl;
import eu.domibus.core.earchive.alerts.FrequencyAlertConfiguration;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_ALERT;
import static eu.domibus.core.alerts.service.EventServiceImpl.ALERT_JMS_LISTENER_CONTAINER_FACTORY;

/**
 * @author idragusa
 * @since 5.0
 */
@Component
public class FrequencyEventListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FrequencyEventListener.class);
    private final AlertService alertService;
    private final DomainContextProvider domainContextProvider;
    private final EventDao eventDao;
    private DatabaseUtil databaseUtil;
    private final EventService eventService;

    public FrequencyEventListener(AlertService alertService, DomainContextProvider domainContextProvider,
                                  EventDao eventDao, DatabaseUtil databaseUtil, EventService eventService) {
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.eventDao = eventDao;
        this.databaseUtil = databaseUtil;
        this.eventService = eventService;
    }

    @JmsListener(containerFactory = ALERT_JMS_LISTENER_CONTAINER_FACTORY, destination = "${" + DOMIBUS_JMS_QUEUE_ALERT + "}",
            selector = "selector ='" + EventType.QueueSelectors.FREQUENCY + "'")
    public void onEvent(final Event event, @Header(name = "DOMAIN", required = false) String domain) {
        saveEventAndTriggerAlert(event, domain);
    }

    private void saveEventAndTriggerAlert(Event event, @Header(name = "DOMAIN") String domain) {
        if (StringUtils.isNotEmpty(domain)) {
            domainContextProvider.setCurrentDomain(domain);
            LOG.debug("Event:[{}] for domain:[{}]", event, domain);
        } else {
            domainContextProvider.clearCurrentDomain();
            LOG.debug("Event:[{}] for super user.", event);
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        FrequencyAlertConfiguration configuration = (FrequencyAlertConfiguration) event.getType().geDefaultAlertType().getConfiguration();

        // need to review
//        String id = event.findStringProperty(EventServiceImpl.EVENT_IDENTIFIER).orElse("");
//        eu.domibus.core.alerts.model.persist.Event entity = eventDao.findWithTypeAndPropertyValue(event.getType(), EventServiceImpl.EVENT_IDENTIFIER, id);
        eu.domibus.core.alerts.model.persist.Event entity = eventService.getOrCreatePersistedEvent(event);
        if (!eventService.shouldCreateAlert(entity, configuration.getFrequency())) {
            return;
        }

        event.setLastAlertDate(LocalDate.now());
        // need to review - always create??
//        eventService.persistEvent(event);
        eventDao.update(entity);

        // todo combine
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }
}
