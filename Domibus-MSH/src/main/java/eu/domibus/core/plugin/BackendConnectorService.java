package eu.domibus.core.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorService.class);

    protected BackendConnectorProvider backendConnectorProvider;
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;

    public BackendConnectorService(BackendConnectorProvider backendConnectorProvider, AsyncNotificationConfigurationService asyncNotificationConfigurationService) {
        this.backendConnectorProvider = backendConnectorProvider;
        this.asyncNotificationConfigurationService = asyncNotificationConfigurationService;
    }

    public List<NotificationType> getRequiredNotificationTypeList(BackendConnector<?, ?> backendConnector) {
        //for backward compatibility purposes
        AsyncNotificationConfiguration asyncNotificationConfiguration = asyncNotificationConfigurationService.getAsyncPluginConfiguration(backendConnector.getName());
        if (isInstanceOfAsyncNotificationConfiguration(asyncNotificationConfiguration)) {
            LOG.debug("Using notification types from the NotificationListener for connector [{}]", backendConnector.getName());
            NotificationListener notificationListener = (NotificationListener) asyncNotificationConfiguration;
            return notificationListener.getRequiredNotificationTypeList();
        }
        return backendConnector.getRequiredNotifications();
    }

    public boolean isInstanceOfAsyncNotificationConfiguration(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        return asyncNotificationConfiguration != null && asyncNotificationConfiguration instanceof NotificationListener;
    }

    public boolean isAbstractBackendConnector(BackendConnector<?, ?> backendConnector) {
        return backendConnector instanceof AbstractBackendConnector;
    }

    public boolean isListerAnInstanceOfAsyncPluginConfiguration(BackendConnector<?, ?> backendConnector) {
        if (!isAbstractBackendConnector(backendConnector)) {
            LOG.trace("Connector [{}] is not an instance of AbstractBackendConnector", backendConnector.getName());
            return false;
        }
        AbstractBackendConnector abstractBackendConnector = (AbstractBackendConnector) backendConnector;
        boolean isAsyncPluginConfiguration = abstractBackendConnector.getLister() instanceof AsyncNotificationConfiguration;
        LOG.trace("MessageLister is an instance of AsyncPluginConfiguration for connector [{}]: [{}]?", backendConnector.getName(), isAsyncPluginConfiguration);
        return isAsyncPluginConfiguration;
    }


}
