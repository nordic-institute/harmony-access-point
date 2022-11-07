package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.dao.EventDao;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_ALERT;
import static eu.domibus.core.alerts.service.EventServiceImpl.ALERT_JMS_LISTENER_CONTAINER_FACTORY;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
public class RepetitiveEventListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RepetitiveEventListener.class);

    private final AlertService alertService;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    private final EventDao eventDao;

    public RepetitiveEventListener(AlertService alertService, DomainContextProvider domainContextProvider, DatabaseUtil databaseUtil, EventDao eventDao) {
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
        this.eventDao = eventDao;
    }

    @JmsListener(containerFactory = ALERT_JMS_LISTENER_CONTAINER_FACTORY, destination = "${"+ DOMIBUS_JMS_QUEUE_ALERT + "}",
            selector = "selector = '" + EventType.QueueSelectors.REPETITIVE + "'")
    @Transactional
    public void onEvent(final Event event, final @Header(name = "DOMAIN", required = false) String domain) {
        triggerAlert(event, domain);
    }

    private void triggerAlert(Event event, final String domain) {
        if (StringUtils.isNotEmpty(domain)) {
            domainContextProvider.setCurrentDomain(domain);
            LOG.debug("Event:[{}] for domain:[{}]", event, domain);
        } else {
            domainContextProvider.clearCurrentDomain();
            LOG.debug("Event:[{}] for super user.", event);
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        eu.domibus.core.alerts.model.persist.Event entity = eventDao.read(event.getEntityId());
        if (entity != null) {
            final Alert alertOnEvent = alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alertOnEvent);
        }
    }

}
