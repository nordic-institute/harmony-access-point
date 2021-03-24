package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AlertListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AlertListener.class);

    private final AlertDispatcherService alertDispatcherService;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    public AlertListener(AlertDispatcherService alertDispatcherService,
                         DomainContextProvider domainContextProvider,
                         DatabaseUtil databaseUtil) {
        this.alertDispatcherService = alertDispatcherService;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
    }

    @JmsListener(containerFactory = "alertJmsListenerContainerFactory", destination = "${domibus.jms.queue.alert}",
            selector = "selector = 'alert'")
    public void onAlert(final Alert alert, @Header(name = "DOMAIN", required = false) String domain) {
        if (StringUtils.isNotEmpty(domain)) {
            domainContextProvider.setCurrentDomain(domain);
            LOG.debug("Alert received:[{}] for domain:[{}]", alert, domain);
        } else {
            domainContextProvider.clearCurrentDomain();
            LOG.debug("Super alert received:[{}]", alert);
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());
        alertDispatcherService.dispatch(alert);
    }

}
