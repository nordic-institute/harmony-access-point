package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandlerFactory;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
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
        Messaging messaging = getMessaging();
        if (messaging == null) {
            LOG.error("Error getting Messaging");
            throw new WebServiceException("Error getting Messaging");
        }

        final IncomingMessageHandler messageHandler = incomingMessageHandlerFactory.getMessageHandler(request, messaging);
        if (messageHandler == null) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Unrecognized message", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw new WebServiceException(ex);
        }
        SOAPMessage soapMessage;
        try {
            soapMessage = messageHandler.processMessage(request, messaging);
        } catch (EbMS3Exception e) {
            LOG.warn("Error processing message!");
            throw new WebServiceException(e);
        }
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

    protected Messaging getMessaging() {
        return (Messaging) PhaseInterceptorChain.getCurrentMessage().get(DispatchClientDefaultProvider.MESSAGING_KEY_CONTEXT_PROPERTY);
    }
}
