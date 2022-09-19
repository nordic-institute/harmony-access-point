package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.core.message.nonrepudiation.SaveRawEnvelopeInterceptor;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.core.message.UserMessageContextKeyProvider.BACKEND_FILTER;
import static eu.domibus.core.message.UserMessageContextKeyProvider.USER_MESSAGE;

/**
 * Interceptor to notify plugin of message response sent
 *
 * @author Ion Perpegel
 * @since 5.0.1
 */
@Service
public class MessageResponseSentBackendNotifierInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageResponseSentBackendNotifierInterceptor.class);

    @Autowired
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Autowired
    private BackendNotificationService backendNotificationService;

    public MessageResponseSentBackendNotifierInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SaveRawEnvelopeInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        BackendFilter matchingBackendFilter = (BackendFilter) userMessageContextKeyProvider.getObjectFromTheCurrentMessage(BACKEND_FILTER);
        UserMessage userMessage = (UserMessage) userMessageContextKeyProvider.getObjectFromTheCurrentMessage(USER_MESSAGE);
        LOG.debug("Notifying plugin of message response sent event for message [{}]", userMessage);

        try {
            backendNotificationService.notifyMessageResponseSent(matchingBackendFilter, userMessage);
        } catch (Throwable ex) {
            // we swallow the exception because it is too late to send an error reply
            LOG.businessWarn(DomibusMessageCode.BUS_NOTIFY_MESSAGE_RESPONSE_SENT_ERROR, matchingBackendFilter.getBackendName(), ex);
        }
    }

}
