package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        BackendFilter matchingBackendFilter = (BackendFilter) userMessageContextKeyProvider.getObjectFromTheCurrentMessage("matchingBackendFilter");
        UserMessage userMessage = (UserMessage) userMessageContextKeyProvider.getObjectFromTheCurrentMessage("userMessage");

        try {
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
        } catch (SubmissionValidationException e) {
            String messageId = userMessage.getMessageId();
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
            EbMS3Exception ex = EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .message(e.getMessage())
                    .refToMessageId(messageId)
                    .cause(e)
                    .build();
            throw new Fault(ex);
        }
    }

}
