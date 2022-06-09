package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.ws.handler.AbstractFaultHandler;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.pmode.NoMatchingPModeFoundException;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.Set;

/**
 * This handler is resposible for creation of ebMS3 conformant error messages
 *
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class FaultInHandler extends AbstractFaultHandler {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(FaultInHandler.class);

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    SoapUtil soapUtil;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
        //Do nothing as this is a fault handler
        return true;
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

        final Exception exception = (Exception) context.get(Exception.class.getName());
        final Throwable cause = exception.getCause();
        EbMS3Exception ebMS3Exception = null;
        final String messageId = (String) PhaseInterceptorChain.getCurrentMessage().getContextualProperty("ebms.messageid");

        if (cause != null) {

            if (!(cause instanceof EbMS3Exception)) {
                //do Mapping of non ebms exceptions
                if (cause instanceof NoMatchingPModeFoundException) {
                    ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                            .message(cause.getMessage())
                            .refToMessageId(((NoMatchingPModeFoundException) cause).getMessageId())
                            .cause(cause)
                            .mshRole(MSHRole.RECEIVING)
                            .build();
                } else {

                    if (cause instanceof WebServiceException) {
                        if (cause.getCause() instanceof EbMS3Exception) {
                            ebMS3Exception = (EbMS3Exception) cause.getCause();
                        }
                    } else {
                        //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain

                        ErrorCode.EbMS3ErrorCode ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0004;
                        String errorMessage = "unknown error occurred";
                        if (cause instanceof WSSecurityException) {
                            errorMessage = cause.getMessage();
                            ebMS3ErrorCode = ErrorCode.EbMS3ErrorCode.EBMS_0103;
                        }

                        ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                                .ebMS3ErrorCode(ebMS3ErrorCode)
                                .message(errorMessage)
                                .refToMessageId(messageId)
                                .cause(cause)
                                .mshRole(MSHRole.RECEIVING)
                                .build();
                    }
                }

            } else {
                ebMS3Exception = (EbMS3Exception) cause;
            }
            if (ebMS3Exception != null){
                if (StringUtils.isBlank(ebMS3Exception.getRefToMessageId()) && StringUtils.isNotBlank(messageId)) {
                    ebMS3Exception.setRefToMessageId(messageId);
                }
        }
            this.processEbMSError(context, ebMS3Exception);

        } else {
            if (exception instanceof PolicyException) {
                //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain

                ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0103)
                        .message(exception.getMessage())
                        .refToMessageId(messageId)
                        .cause(exception)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            } else {
                ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                        .message("unknown error occurred")
                        .refToMessageId(messageId)
                        .mshRole(MSHRole.RECEIVING)
                        .build();
            }

            this.processEbMSError(context, ebMS3Exception);
        }


        return true;
    }

    private void processEbMSError(final SOAPMessageContext context, final EbMS3Exception ebMS3Exception) {

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
        context.setMessage(soapMessageWithEbMS3Error);

        final Ebms3Messaging ebms3Messaging = this.extractMessaging(soapMessageWithEbMS3Error);
        if(ebms3Messaging == null) {
            LOG.trace("Messaging header is null, error log not created");
            return;
        }
        final String senderParty = LOG.getMDC(IDomibusLogger.MDC_FROM);
        final String receiverParty = LOG.getMDC(IDomibusLogger.MDC_TO);
        final String service = LOG.getMDC(IDomibusLogger.MDC_SERVICE);
        final String action = LOG.getMDC(IDomibusLogger.MDC_ACTION);

        final Boolean testMessage = userMessageHandlerService.checkTestMessage(service, action);
        LOG.businessError(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_RECEIVE_FAILED : DomibusMessageCode.BUS_MESSAGE_RECEIVE_FAILED, ebMS3Exception, senderParty, receiverParty, ebms3Messaging.getSignalMessage().getMessageInfo().getMessageId());

        //log the raw xml Signal message
        soapUtil.logRawXmlMessageWhenEbMS3Error(soapMessageWithEbMS3Error);

        errorLogService.createErrorLog(ebms3Messaging, MSHRole.RECEIVING, null);
    }

    @Override
    public void close(final MessageContext context) {

    }
}
