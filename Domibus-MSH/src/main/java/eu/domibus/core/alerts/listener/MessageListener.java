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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class MessageListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DatabaseUtil databaseUtil;

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'message'")
    @Transactional
    public void onMessageEvent(final Event event,@Header(name = "DOMAIN") String domain) {
        LOG.debug("Message event received:[{}]", event);
        domainContextProvider.setCurrentDomain(domain);
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
        eventService.enrichMessageEvent(event);
        eventService.persistEvent(event);
        final Alert alertOnEvent = alertService.createAlertOnEvent(event);
        alertService.enqueueAlert(alertOnEvent);
    }

}
