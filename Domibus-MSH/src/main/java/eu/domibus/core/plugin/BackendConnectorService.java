package eu.domibus.core.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorService.class);

    protected BackendConnectorHelper backendConnectorHelper;
    protected BackendConnectorProvider backendConnectorProvider;
    protected RoutingService routingService;

    public BackendConnectorService(BackendConnectorHelper backendConnectorHelper, BackendConnectorProvider backendConnectorProvider, RoutingService routingService) {
        this.backendConnectorHelper = backendConnectorHelper;
        this.backendConnectorProvider = backendConnectorProvider;
        this.routingService = routingService;
    }

    public List<NotificationType> getRequiredNotificationTypeList(BackendConnector<?, ?> backendConnector) {
        //for backward compatibility purposes
        AsyncNotificationListener asyncNotificationListener = routingService.getNotificationListener(backendConnector.getName());
        if (asyncNotificationListener != null && asyncNotificationListener instanceof NotificationListener) {
            LOG.debug("Using notification types from the NotificationListener for connector [{}]", backendConnector.getName());
            NotificationListener notificationListener = (NotificationListener) asyncNotificationListener;
            return notificationListener.getRequiredNotificationTypeList();
        }
        return backendConnector.getRequiredNotifications();
    }


}
