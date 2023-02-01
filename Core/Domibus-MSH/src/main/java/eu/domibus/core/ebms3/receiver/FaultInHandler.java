package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.ws.handler.AbstractFaultHandler;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.TestMessageValidator;
import eu.domibus.core.message.UserMessageErrorCreator;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.NoMatchingPModeFoundException;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * This handler is responsible for creation of ebMS3 conformant error messages
 *
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class FaultInHandler extends AbstractFaultHandler {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FaultInHandler.class);

    public static final String UNKNOWN_ERROR_OCCURRED = "unknown error occurred";

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    protected TestMessageValidator testMessageValidator;

    @Autowired
    SoapUtil soapUtil;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    UserMessageErrorCreator userMessageErrorCreator;

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
        //Do nothing as this is a fault handler
        return true;
    }

    @Override
    public void close(final MessageContext context) {
    }

    /**
     * The {@code handleFault} method is responsible for handling and conversion of exceptions
     * thrown during the processing of incoming ebMS3 messages
     */
    @Override
    public boolean handleFault(final SOAPMessageContext context) {
        if (context == null) {
            LOG.error("Context is null and shouldn't be");
            throw new MissingResourceException("Context is null and shouldn't be", SOAPMessageContext.class.getName(), "context");
        }

        final String messageId = (String) PhaseInterceptorChain.getCurrentMessage().getContextualProperty("ebms.messageid");
        final Exception exception = (Exception) context.get(Exception.class.getName());
        EbMS3Exception ebMS3Exception = getEBMS3Exception(exception, messageId);

        SOAPMessage soapMessageWithEbMS3Error = getSoapMessage(ebMS3Exception);
        context.setMessage(soapMessageWithEbMS3Error);

        soapUtil.logRawXmlMessageWhenEbMS3Error(soapMessageWithEbMS3Error);

        updateErrorLog(soapMessageWithEbMS3Error, ebMS3Exception);

        notifyPlugins(ebMS3Exception);

        return true;
    }

    private EbMS3Exception getEBMS3Exception(Exception exception, String messageId) {
        EbMS3Exception ebMS3Exception = null;

        final Throwable cause = exception.getCause();

        if (cause != null) {
            //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain
            if (!(cause instanceof EbMS3Exception)) {
                //do mapping of non ebms exceptions
                if (cause instanceof NoMatchingPModeFoundException) {
                    ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                            .message(cause.getMessage())
                            .refToMessageId(((NoMatchingPModeFoundException) cause).getMessageId())
                            .cause(cause)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                } else if (cause instanceof WebServiceException) {
                    if (cause.getCause() instanceof EbMS3Exception) {
                        ebMS3Exception = (EbMS3Exception) cause.getCause();
                    } else {
                        Throwable ex = cause.getCause();
                        ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                                .message(ex.getMessage())
                                .refToMessageId(messageId)
                                .cause(ex)
                                .mshRole(MSHRole.RECEIVING)
                                .build();
                    }
                } else if (cause instanceof CertificateException || cause instanceof InvalidCanonicalizerException) {
                    ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0101)
                            .message(cause.getMessage())
                            .refToMessageId(messageId)
                            .cause(cause)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                } else if (cause instanceof WSSecurityException) {
                    WSSecurityException wsSecurityException = (WSSecurityException) cause;
                    ErrorCode.EbMS3ErrorCode ebMS3ErrorCode;
                    switch (wsSecurityException.getErrorCode()) {
                        case FAILED_CHECK:
                            ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0102; //The signature or decryption was invalid
                            break;
                        case FAILED_AUTHENTICATION:
                            ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0101;
                            break;
                        default:
                            ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0103;
                    }
                    LOG.error("Security exception encountered with ebMS3 error code: [{}]", ebMS3ErrorCode);
                    ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ebMS3ErrorCode)
                            .message(wsSecurityException.getMessage())
                            .refToMessageId(messageId)
                            .cause(cause)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                } else {
                    ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                            .message(UNKNOWN_ERROR_OCCURRED)
                            .refToMessageId(messageId)
                            .cause(cause)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                }
            } else {
                ebMS3Exception = (EbMS3Exception) cause;
            }
        } else {
            // no cause
            if (exception instanceof PolicyException) {
                //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain
                ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0103)
                        .message(exception.getMessage())
                        .refToMessageId(messageId)
                        .cause(exception)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            } else if (exception instanceof SoapFault) {
                SoapFault fault = (SoapFault) exception;
                ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                        .message(fault.getMessage())
                        .refToMessageId(messageId)
                        .cause(fault)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            } else {
                ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                        .message(UNKNOWN_ERROR_OCCURRED)
                        .refToMessageId(messageId)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            }
        }

        if (ebMS3Exception != null) {
            if (StringUtils.isBlank(ebMS3Exception.getRefToMessageId()) && StringUtils.isNotBlank(messageId)) {
                ebMS3Exception.setRefToMessageId(messageId);
            }
        }

        return ebMS3Exception;
    }

    private SOAPMessage getSoapMessage(EbMS3Exception ebMS3Exception) {
        if (ebMS3Exception == null) {
            LOG.warn("ebMSException is null on this stage and shouldn't");
            throw new MissingResourceException("ebMSException is null on this stage and shouldn't", EbMS3Exception.class.getName(), "ebMS3Exception");
        }

        // at this point an EbMS3Exception is available in any case
        SOAPMessage soapMessageWithEbMS3Error = null;
        try {
            soapMessageWithEbMS3Error = this.messageBuilder.buildSOAPFaultMessage(ebMS3Exception.getFaultInfoError());
        } catch (final EbMS3Exception e) {
            errorLogService.createErrorLog(e, MSHRole.RECEIVING, null);
        }
        return soapMessageWithEbMS3Error;
    }

    private void updateErrorLog(SOAPMessage soapMessageWithEbMS3Error, EbMS3Exception ebMS3Exception) {
        final Ebms3Messaging ebms3Messaging = this.extractMessaging(soapMessageWithEbMS3Error);
        if (ebms3Messaging == null) {
            LOG.trace("Messaging header is null, error log not created");
            return;
        }
        final String senderParty = LOG.getMDC(DomibusLogger.MDC_FROM);
        final String receiverParty = LOG.getMDC(DomibusLogger.MDC_TO);
        final String service = LOG.getMDC(DomibusLogger.MDC_SERVICE);
        final String action = LOG.getMDC(DomibusLogger.MDC_ACTION);

        final Boolean testMessage = testMessageValidator.checkTestMessage(service, action);
        LOG.businessError(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_RECEIVE_FAILED : DomibusMessageCode.BUS_MESSAGE_RECEIVE_FAILED, ebMS3Exception, senderParty, receiverParty, ebms3Messaging.getSignalMessage().getMessageInfo().getMessageId());

        errorLogService.createErrorLog(ebms3Messaging, MSHRole.RECEIVING, null);
    }

    private void notifyPlugins(EbMS3Exception faultCause) {
        LOG.debug("Preparing message details for plugin notification about the receive failure");
        Ebms3Messaging ebms3Messaging = (Ebms3Messaging) PhaseInterceptorChain.getCurrentMessage().getExchange().get(MessageConstants.EMBS3_MESSAGING_OBJECT);
        UserMessage userMessage = null;
        if (ebms3Messaging.getUserMessage() != null) {
            userMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getUserMessage());
        }

        final Map<String, String> properties = new HashMap<>();
        if (faultCause.getErrorCode() != null) {
            properties.put(MessageConstants.ERROR_CODE, faultCause.getErrorCode().name());
        }
        properties.put(MessageConstants.ERROR_DETAIL, faultCause.getErrorDetail());
        backendNotificationService.fillEventProperties(userMessage, properties);
        backendNotificationService.notifyMessageReceivedFailure(userMessage, userMessageErrorCreator.createErrorResult(faultCause));
        LOG.debug("Plugins notified about failure to receive message with id: [{}]", Optional.ofNullable(userMessage).map(UserMessage::getMessageId).orElse(null));
    }

}
