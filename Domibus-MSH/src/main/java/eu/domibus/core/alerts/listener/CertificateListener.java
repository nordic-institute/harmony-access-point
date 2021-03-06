package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class CertificateListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CertificateListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DatabaseUtil databaseUtil;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'certificateImminentExpiration' or selector = 'certificateExpired'")
    public void onCertificateEvent(final Event event, @Header(name = "DOMAIN") String domain) {
        saveEventAndTriggerAlert(event, domain);
    }

    private void saveEventAndTriggerAlert(Event event, @Header(name = "DOMAIN") String domain) {
        LOG.debug("Certificate event:[{}] for domain:[{}]", event, domain);
        domainContextProvider.setCurrentDomain(domain);
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
