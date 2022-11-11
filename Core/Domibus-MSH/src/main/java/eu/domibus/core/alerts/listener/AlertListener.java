package eu.domibus.core.alerts.listener;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.service.AlertDispatcherService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_JMS_QUEUE_ALERT;
import static eu.domibus.core.alerts.service.EventServiceImpl.ALERT_JMS_LISTENER_CONTAINER_FACTORY;

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

    @JmsListener(containerFactory = ALERT_JMS_LISTENER_CONTAINER_FACTORY, destination = "${"+ DOMIBUS_JMS_QUEUE_ALERT + "}",
            selector = "selector = 'alert'")
    public void onAlert(final Alert alert, @Header(name = MessageConstants.DOMAIN, required = false) String domain) {
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
