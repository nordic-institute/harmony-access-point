package eu.domibus.plugin;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
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
    public void notifyPlugin(BackendConnector backendConnector, String messageId, Map<String, Object> properties) {
        ErrorResultImpl errorResult = getErrorResult(messageId, properties);

        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();
        event.setMessageId(messageId);
        String service = (String) properties.get(MessageConstants.SERVICE);
        event.setService(service);

        String serviceType = (String) properties.get(MessageConstants.SERVICE_TYPE);
        event.setServiceType(serviceType);

        String action = (String) properties.get(MessageConstants.ACTION);
        event.setAction(action);

        event.setErrorResult(errorResult);
        event.setEndpoint((String) properties.get(MessageConstants.ENDPOINT));
        backendConnectorDelegate.messageReceiveFailed(backendConnector, event);
    }

    protected ErrorResultImpl getErrorResult(String messageId, Map<String, Object> properties) {
        final String errorCode = (String) properties.get(MessageConstants.ERROR_CODE);
        final String errorDetail = (String) properties.get(MessageConstants.ERROR_DETAIL);
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
