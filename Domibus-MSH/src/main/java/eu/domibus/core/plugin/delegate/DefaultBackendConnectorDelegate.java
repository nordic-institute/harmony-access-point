package eu.domibus.core.plugin.delegate;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
public class DefaultBackendConnectorDelegate implements BackendConnectorDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultBackendConnectorDelegate.class);

    @Autowired
    ClassUtil classUtil;

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

    public void deliverMessage(BackendConnector backendConnector, DeliverMessageEvent event) {
        if (classUtil.isMethodDefined(backendConnector, "deliverMessage", new Class[]{DeliverMessageEvent.class})) {
            LOG.trace("Calling deliverMessage method");
            backendConnector.deliverMessage(event);
        }
        LOG.trace("Calling deprecated deliverMessage method");
        backendConnector.deliverMessage(event.getMessageId());
    }

    public void messageSendFailed(BackendConnector backendConnector, MessageSendFailedEvent event) {
        if (classUtil.isMethodDefined(backendConnector, "messageSendFailed", new Class[]{MessageSendFailedEvent.class})) {
            LOG.trace("Calling messageSendFailed method");
            backendConnector.messageSendFailed(event);
        }
        LOG.trace("Calling deprecated messageSendFailed method");
        backendConnector.messageSendFailed(event.getMessageId());

    }

    public void messageSendSuccess(BackendConnector backendConnector, MessageSendSuccessEvent event) {
        if (classUtil.isMethodDefined(backendConnector, "messageSendSuccess", new Class[]{MessageSendSuccessEvent.class})) {
            LOG.trace("Calling messageSendSuccess method");
            backendConnector.messageSendSuccess(event);
        }
        LOG.trace("Calling deprecated messageSendSuccess method");
        backendConnector.messageSendSuccess(event.getMessageId());
    }
}
