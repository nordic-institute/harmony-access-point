package eu.domibus.core.plugin.notification;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageReceivedFailureNotifier implements PluginEventNotifier {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginMessageReceivedFailureNotifier.class);

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageReceivedFailureNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_RECEIVED_FAILURE == notificationType;
    }

    @Override
    public void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, String> properties) {
        ErrorResultImpl errorResult = getErrorResult(messageId, properties);

        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();
        event.setMessageId(messageId);
        String service = properties.get(MessageConstants.SERVICE);
        event.setService(service);

        String serviceType = properties.get(MessageConstants.SERVICE_TYPE);
        event.setServiceType(serviceType);

        String action = properties.get(MessageConstants.ACTION);
        event.setAction(action);

        event.setErrorResult(errorResult);
        event.setEndpoint(properties.get(MessageConstants.ENDPOINT));
        backendConnectorDelegate.messageReceiveFailed(backendConnector, event);
    }

    protected ErrorResultImpl getErrorResult(String messageId, Map<String, String> properties) {
        final String errorCode = properties.get(MessageConstants.ERROR_CODE);
        final String errorDetail = properties.get(MessageConstants.ERROR_DETAIL);
        ErrorResultImpl errorResult = new ErrorResultImpl();
        try {
            errorResult.setErrorCode(ErrorCode.findBy(errorCode));
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find error code for [" + errorCode + "]");
        }
        errorResult.setErrorDetail(errorDetail);
        errorResult.setMessageInErrorId(messageId);
        return errorResult;
    }
}
