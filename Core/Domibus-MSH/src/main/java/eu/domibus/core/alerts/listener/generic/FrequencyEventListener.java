package eu.domibus.core.alerts.listener.generic;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.generic.FrequencyAlertConfiguration;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
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
    private final DatabaseUtil databaseUtil;
    private final EventService eventService;
    private final AlertConfigurationService alertConfigurationService;

    public FrequencyEventListener(AlertService alertService, DomainContextProvider domainContextProvider,
                                  EventDao eventDao, DatabaseUtil databaseUtil, EventService eventService,
                                  AlertConfigurationService alertConfigurationService) {
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.eventDao = eventDao;
        this.databaseUtil = databaseUtil;
        this.eventService = eventService;
        this.alertConfigurationService = alertConfigurationService;
    }

    @JmsListener(containerFactory = ALERT_JMS_LISTENER_CONTAINER_FACTORY, destination = "${" + DOMIBUS_JMS_QUEUE_ALERT + "}",
            selector = "selector ='" + EventType.QueueSelectors.FREQUENCY + "'")
    public void onEvent(final Event event, @Header(name = MessageConstants.DOMAIN, required = false) String domain) {
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

        FrequencyAlertConfiguration configuration = (FrequencyAlertConfiguration) alertConfigurationService.getConfiguration(event.getType().geDefaultAlertType());

        eu.domibus.core.alerts.model.persist.Event entity = eventService.getOrCreatePersistedEvent(event);
        if (!eventService.shouldCreateAlert(entity, configuration.getFrequency())) {
            LOG.info("No alert is created for event [{}] with entity [{}] and frequuency [{}]", event, entity, configuration.getFrequency());
            return;
        }

        entity.setLastAlertDate(LocalDate.now());
        eventDao.update(entity);

        alertService.createAndEnqueueAlertOnEvent(event);
    }
}
