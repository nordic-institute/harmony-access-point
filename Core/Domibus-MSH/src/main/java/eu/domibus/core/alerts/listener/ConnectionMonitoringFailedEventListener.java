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
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
@Component
public class ConnectionMonitoringFailedEventListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ConnectionMonitoringFailedEventListener.class);

    private final AlertService alertService;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    private final EventDao eventDao;

    public ConnectionMonitoringFailedEventListener(AlertService alertService, DomainContextProvider domainContextProvider, DatabaseUtil databaseUtil, EventDao eventDao) {
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
        this.eventDao = eventDao;
    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = '" + EventType.QuerySelectors.CONNECTION_MONITORING_FAILURE + "'")
    @Transactional
    public void onEvent(final Event event, final @Header(name = "DOMAIN", required = false) String domain) {
        saveEventAndTriggerAlert(event, domain);
    }

    // todo all these listener classes seem to fall into 2 categories, so a base class could be foreseen (or the use of the same listener for more than one event)
    // EDELIVERY-9241 Ion Perpegel 16/09/22
    private void saveEventAndTriggerAlert(Event event, final String domain) {
        LOG.debug("Connection Monitoring Falure event received:[{}]", event);
        domainContextProvider.setCurrentDomain(domain);
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        eu.domibus.core.alerts.model.persist.Event entity = eventDao.read(event.getEntityId());
        if (entity != null) {
            final Alert alertOnEvent = alertService.createAlertOnEvent(event);
            alertService.enqueueAlert(alertOnEvent);
        }
    }

}
