package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandlerFactory;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;


/**
 * This method is responsible for the receiving of ebMS3 messages and the sending of signal messages like receipts or ebMS3 errors in return
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */

@WebServiceProvider(portName = "mshPort", serviceName = "mshService")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHWebservice implements Provider<SOAPMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebservice.class);

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected IncomingMessageHandlerFactory incomingMessageHandlerFactory;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Timer(clazz = MSHWebservice.class,value = "incoming_user_message")
    @Counter(clazz = MSHWebservice.class,value = "incoming_user_message")
    @Override
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.trace("Message received");
        setCurrentDomain(request);
        Ebms3Messaging ebms3Messaging = getMessaging();
        if (ebms3Messaging == null) {
            LOG.error("Error getting Messaging");
            throw new WebServiceException("Error getting Messaging");
        }

        final IncomingMessageHandler messageHandler = incomingMessageHandlerFactory.getMessageHandler(request, ebms3Messaging);
        if (messageHandler == null) {
            throw new WebServiceException( EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message("Unrecognized message")
                    .refToMessageId(ebms3Messaging.getUserMessage().getMessageInfo().getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build());
        }
        SOAPMessage soapMessage;
        try {
            soapMessage = messageHandler.processMessage(request, ebms3Messaging);
        } catch (EbMS3Exception e) {
            LOG.warn("Error processing message!");
            throw new WebServiceException(e);
        }
        setUserMessageEntityIdOnContext();

        return soapMessage;

    }

    protected void setCurrentDomain(final SOAPMessage request) {
        LOG.trace("Setting the current domain");
        try {
            final String domainCode = (String)request.getProperty(DomainContextProvider.HEADER_DOMIBUS_DOMAIN);
            domainContextProvider.setCurrentDomain(domainCode);
        } catch (SOAPException se) {
            throw new DomainTaskException("Could not get current domain from request header " + DomainContextProvider.HEADER_DOMIBUS_DOMAIN, se);
        }
    }

    protected void setUserMessageEntityIdOnContext() {
        final String userMessageEntityId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID);
        PhaseInterceptorChain.getCurrentMessage().getExchange().put(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY, userMessageEntityId);
        LOG.debug("PUT UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY [{}]", userMessageEntityId);
    }

    protected Ebms3Messaging getMessaging() {
        return (Ebms3Messaging) PhaseInterceptorChain.getCurrentMessage().get(DispatchClientDefaultProvider.MESSAGING_KEY_CONTEXT_PROPERTY);
    }
}
