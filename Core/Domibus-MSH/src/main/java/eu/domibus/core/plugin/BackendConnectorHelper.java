package eu.domibus.core.plugin;

import eu.domibus.common.NotificationType;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorHelper {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorHelper.class);

    public List<NotificationType> getRequiredNotificationTypeList(BackendConnector<?, ?> backendConnector) {
        return backendConnector.getRequiredNotifications();
    }

    public boolean isAbstractBackendConnector(BackendConnector<?, ?> backendConnector) {
        return backendConnector instanceof AbstractBackendConnector;
    }


}
