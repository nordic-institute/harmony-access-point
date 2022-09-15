package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.core.message.nonrepudiation.SaveRawEnvelopeInterceptor;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.messaging.MessageConstants.BACKEND_FILTER;
import static eu.domibus.messaging.MessageConstants.USER_MESSAGE;

/**
 * Interceptor to notify plugin of message received
 *
 * @author Ion Perpegel
 * @since 5.0.1
 */
@Service
public class MessageReceivedBackendNotifierInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageReceivedBackendNotifierInterceptor.class);

    @Autowired
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Autowired
    private BackendNotificationService backendNotificationService;

    public MessageReceivedBackendNotifierInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SaveRawEnvelopeInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        BackendFilter matchingBackendFilter = (BackendFilter) userMessageContextKeyProvider.getObjectFromTheCurrentMessage(BACKEND_FILTER);
        UserMessage userMessage = (UserMessage) userMessageContextKeyProvider.getObjectFromTheCurrentMessage(USER_MESSAGE);
        LOG.debug("Notifying plugin of message received event for message [{}]", userMessage);

        try {
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
        } catch (Exception ex) {
            throw new Fault(ex);
        }
    }

}
