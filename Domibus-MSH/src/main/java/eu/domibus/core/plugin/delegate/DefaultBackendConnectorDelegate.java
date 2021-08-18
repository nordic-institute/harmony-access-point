package eu.domibus.core.plugin.delegate;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.*;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.BackendConnectorService;
import eu.domibus.core.plugin.notification.AsyncNotificationConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Service
public class DefaultBackendConnectorDelegate implements BackendConnectorDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultBackendConnectorDelegate.class);


    protected ClassUtil classUtil;
    protected AsyncNotificationConfigurationService asyncNotificationConfigurationService;
    protected BackendConnectorProvider backendConnectorProvider;
    protected BackendConnectorService backendConnectorService;

    public DefaultBackendConnectorDelegate(ClassUtil classUtil,
                                           AsyncNotificationConfigurationService asyncNotificationConfigurationService,
                                           BackendConnectorProvider backendConnectorProvider,
                                           BackendConnectorService backendConnectorService) {
        this.classUtil = classUtil;
        this.asyncNotificationConfigurationService = asyncNotificationConfigurationService;
        this.backendConnectorProvider = backendConnectorProvider;
        this.backendConnectorService = backendConnectorService;
    }

    @Override
    public void messageStatusChanged(BackendConnector backendConnector, MessageStatusChangeEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Notifying connector [{}] about status change event [{}]", backendConnector.getName(), event);
        }
        backendConnector.messageStatusChanged(event);
    }

    @Override
    public void messageReceiveFailed(BackendConnector backendConnector, MessageReceiveFailureEvent event) {
        LOG.info("Calling messageReceiveFailed method");
        backendConnector.messageReceiveFailed(event);
    }

    @Override
    public void deliverMessage(BackendConnector backendConnector, DeliverMessageEvent event) {
        LOG.trace("Calling deliverMessage method");
        backendConnector.deliverMessage(event);
    }

    @Override
    public void messageSendFailed(BackendConnector backendConnector, MessageSendFailedEvent event) {
        LOG.trace("Calling messageSendFailed method");
        backendConnector.messageSendFailed(event);
    }

    @Override
    public void messageSendSuccess(BackendConnector backendConnector, MessageSendSuccessEvent event) {
        LOG.trace("Calling messageSendSuccess method");
        backendConnector.messageSendSuccess(event);
    }

    @Override
    public void messageDeletedEvent(String backend, MessageDeletedEvent event) {
        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backend);
        if (backendConnector == null) {
            LOG.warn("Could not find connector for backend [{}]", backend);
            return;
        }
        backendConnector.messageDeletedEvent(event);
    }

    @Override
    public void messageDeletedBatchEvent(String backend, MessageDeletedBatchEvent event) {
        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backend);
        if (backendConnector == null) {
            LOG.warn("Could not find connector for backend [{}]", backend);
            return;
        }
        backendConnector.messageDeletedBatchEvent(event);
    }

}
