package eu.domibus.core.plugin;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorHelper.class);

    public boolean isAbstractBackendConnector(BackendConnector<?, ?> backendConnector) {
        return backendConnector instanceof AbstractBackendConnector;
    }

    public boolean isListerAnInstanceOfNotificationListener(BackendConnector<?, ?> backendConnector) {
        if (!isAbstractBackendConnector(backendConnector)) {
            LOG.trace("Connector [{}] is not an instance of AbstractBackendConnector", backendConnector.getName());
            return false;
        }
        AbstractBackendConnector abstractBackendConnector = (AbstractBackendConnector) backendConnector;
        boolean isAsyncNotificationListener = abstractBackendConnector.getLister() instanceof AsyncNotificationListener;
        LOG.trace("MessageLister is an instance of AsyncNotificationListener for connector [{}]: [{}]?", backendConnector.getName(), isAsyncNotificationListener);
        return isAsyncNotificationListener;
    }
}
