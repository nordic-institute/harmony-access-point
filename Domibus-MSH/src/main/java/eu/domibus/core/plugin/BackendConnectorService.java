package eu.domibus.core.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.BackendConnectorProvider;
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
        return backendConnector.getRequiredNotifications();
    }

    public boolean isAbstractBackendConnector(BackendConnector<?, ?> backendConnector) {
        return backendConnector instanceof AbstractBackendConnector;
    }


}
