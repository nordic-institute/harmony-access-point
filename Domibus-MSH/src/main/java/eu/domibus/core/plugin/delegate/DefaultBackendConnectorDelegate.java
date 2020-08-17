package eu.domibus.core.plugin.delegate;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.*;
import eu.domibus.core.plugin.BackendConnectorHelper;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.notification.AsyncNotificationListener;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Service
public class DefaultBackendConnectorDelegate implements BackendConnectorDelegate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultBackendConnectorDelegate.class);


    protected ClassUtil classUtil;
    protected RoutingService routingService;
    protected BackendConnectorProvider backendConnectorProvider;
    protected BackendConnectorHelper backendConnectorHelper;

    public DefaultBackendConnectorDelegate(ClassUtil classUtil,
                                           RoutingService routingService,
                                           BackendConnectorProvider backendConnectorProvider,
                                           BackendConnectorHelper backendConnectorHelper) {
        this.classUtil = classUtil;
        this.routingService = routingService;
        this.backendConnectorProvider = backendConnectorProvider;
        this.backendConnectorHelper = backendConnectorHelper;
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
        if (classUtil.isMethodDefined(backendConnector, "deliverMessage", new Class[]{DeliverMessageEvent.class})) {
            LOG.trace("Calling deliverMessage method");
            backendConnector.deliverMessage(event);
        } else {
            LOG.trace("Calling deprecated deliverMessage method");
            backendConnector.deliverMessage(event.getMessageId());
        }
    }

    @Override
    public void messageSendFailed(BackendConnector backendConnector, MessageSendFailedEvent event) {
        if (classUtil.isMethodDefined(backendConnector, "messageSendFailed", new Class[]{MessageSendFailedEvent.class})) {
            LOG.trace("Calling messageSendFailed method");
            backendConnector.messageSendFailed(event);
        } else {
            LOG.trace("Calling deprecated messageSendFailed method");
            backendConnector.messageSendFailed(event.getMessageId());
        }

    }

    @Override
    public void messageSendSuccess(BackendConnector backendConnector, MessageSendSuccessEvent event) {
        if (classUtil.isMethodDefined(backendConnector, "messageSendSuccess", new Class[]{MessageSendSuccessEvent.class})) {
            LOG.trace("Calling messageSendSuccess method");
            backendConnector.messageSendSuccess(event);
        } else {
            LOG.trace("Calling deprecated messageSendSuccess method");
            backendConnector.messageSendSuccess(event.getMessageId());
        }
    }

    @Override
    public void messageDeletedEvent(String backend, MessageDeletedEvent event) {
        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backend);
        if (backendConnector == null) {
            LOG.warn("Could not find connector for backend [{}]", backend);
            return;
        }
        backendConnector.messageDeletedEvent(event);

        //for backward compatibility purposes
        callNotificationListerForMessageDeletedEvent(backendConnector, event);
    }

    /**
     * Call the NotificationLister if needed to maintain the backward compatibility
     *
     * @param backendConnector The backend connector associated with the NotificationListener
     * @param event            the message deleted event details
     */
    protected void callNotificationListerForMessageDeletedEvent(BackendConnector<?, ?> backendConnector, MessageDeletedEvent event) {
        if (!shouldCallNotificationListerForMessageDeletedEvent(backendConnector)) {
            return;
        }
        AsyncNotificationListener asyncNotificationListener = routingService.getNotificationListener(backendConnector.getName());
        if (asyncNotificationListener == null && asyncNotificationListener instanceof NotificationListener) {
            NotificationListener notificationListener = (NotificationListener) asyncNotificationListener;
            LOG.debug("Calling NotificationListener for message deletion callback for connector [{}]", backendConnector.getName());
            notificationListener.deleteMessageCallback(event.getMessageId());
        }
    }

    protected boolean shouldCallNotificationListerForMessageDeletedEvent(BackendConnector<?, ?> backendConnector) {
        if (backendConnectorHelper.isListerAnInstanceOfNotificationListener(backendConnector)) {
            LOG.debug("No need to call the notification listener for connector [{}]; already called by AbstractBackendConnector", backendConnector.getName());
            return false;
        }
        LOG.debug("MessageLister is not an instance of NotificationListener. We need to call the notification listener for connector [{}]", backendConnector.getName());
        return true;
    }

}
