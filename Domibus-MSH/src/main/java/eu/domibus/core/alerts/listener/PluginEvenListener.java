package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener to handle plugin event and create alert
 *
 * @author François Gautier
 * @since 5.0
 */
@Component
public class PluginEvenListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginEvenListener.class);

    private final EventService eventService;

    private final AlertService alertService;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    public PluginEvenListener(EventService eventService,
                              AlertService alertService,
                              DomainContextProvider domainContextProvider,
                              DatabaseUtil databaseUtil) {
        this.eventService = eventService;
        this.alertService = alertService;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'PLUGIN_EVENT'")
    @Transactional
    public void onPluginEvent(final Event event, final @Header(name = "DOMAIN", required = false) String domain) {
        saveEventAndTriggerAlert(event, domain);
    }

    private void saveEventAndTriggerAlert(Event event, final String domain) {
        if (StringUtils.isNotEmpty(domain)) {
            domainContextProvider.setCurrentDomain(domain);
            LOG.debug("Authentication event:[{}] for domain:[{}].", event, domain);
        } else {
            domainContextProvider.clearCurrentDomain();
            LOG.debug("Authentication event:[{}] for super user.", event);
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
        eventService.persistEvent(event);
        alertService.createAndEnqueueAlertOnPluginEvent(event);
    }

}
