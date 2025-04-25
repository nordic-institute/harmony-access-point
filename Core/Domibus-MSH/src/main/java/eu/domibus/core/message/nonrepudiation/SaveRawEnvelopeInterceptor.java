package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 * Interceptor to save the raw xml envelope:
 * - the outgoing user message in case of a pulled message
 * - the outgoing signal message in case of a pushed message
 * The non repudiation mechanism needs the raw message at the end of the interceptor queue, as it needs the security header added
 */
@Service
public class SaveRawEnvelopeInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRawEnvelopeInterceptor.class);

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Autowired
    protected SoapUtil soapUtil;

    public SaveRawEnvelopeInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SoapOutInterceptor.SoapOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        if (!shouldSaveOutEnvelope(message.getExchange())) {
            return; // there is no out envelope to save, more info was already logged inside the method
        }
        MessageType messageType = (MessageType) message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        Long messageEntityId = getMessageEntityId(message.getExchange());

        String ebmsMessageId = (String) message.getExchange().get(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY);
        String outgoingUserMessageId = (String) message.getExchange().get(DispatchClientDefaultProvider.MESSAGE_ID);

        if (messageType == MessageType.USER_MESSAGE) {
            LOG.info("Saving the outgoing message raw xml envelope: message type is [{}]; outgoing user message id: [{}] with message entity id: [{}]; in response to incoming message id: [{}]", messageType, outgoingUserMessageId, messageEntityId, ebmsMessageId);
        } else if (messageType == MessageType.SIGNAL_MESSAGE) {
            LOG.info("Saving the outgoing message raw xml envelope: message type is [{}]; in response to incoming user message id: [{}] with message entity id: [{}]", messageType, ebmsMessageId, messageEntityId);
        }

        try {
            SOAPMessage soapContent = message.getContent(SOAPMessage.class);
            String rawXMLMessage = soapUtil.getRawXMLMessage(soapContent);

            if (messageType == MessageType.USER_MESSAGE) {
                nonRepudiationService.saveUserMessageRawEnvelope(rawXMLMessage, messageEntityId);
                LOG.debug("Saved the outgoing user message envelope for user message id [{}], entity id [{}]", outgoingUserMessageId, messageEntityId);
            } else if (messageType == MessageType.SIGNAL_MESSAGE) {
                nonRepudiationService.saveSignalMessageRawEnvelope(rawXMLMessage, messageEntityId);
                LOG.debug("Saved the outgoing signal message envelope for user message id [{}], entity id [{}]", ebmsMessageId, messageEntityId);
            } else {
                LOG.error("Unknown message type: [{}] for message entity id [{}]", messageType, messageEntityId);
            }
        } catch (TransformerException e) {
            throw new WebServiceException(new IllegalArgumentException(e));
        } catch (Exception e) { //saving the raw envelope should not prevent the successful exchange of messages
            LOG.error("Could not save outgoing message raw envelope for message entity id [{}]", messageEntityId, e);
        }
    }

    private Long getMessageEntityId(Exchange exchange) {
        String messageEntityIdValue = (String) exchange.get(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY);
        return StringUtils.isBlank(messageEntityIdValue) ? null : Long.valueOf(messageEntityIdValue);
    }

    private boolean shouldSaveOutEnvelope(Exchange exchange) {
        MessageType messageType = (MessageType) exchange.get(MSHDispatcher.MESSAGE_TYPE_OUT);
        Long messageEntityId = getMessageEntityId(exchange);
        boolean duplicateMessage = BooleanUtils.toBoolean(userMessageContextKeyProvider.getKeyFromTheCurrentMessage(UserMessage.USER_MESSAGE_DUPLICATE_KEY));
        if (duplicateMessage) {
            LOG.debug("Skip saving the outgoing message raw xml envelope for duplicate message: message type is [{}]; user message entity id: [{}]", messageType, messageEntityId);
            return false;
        }
        if (messageType != null && messageEntityId != null) {
            // we have the out message type and the message entity id, we'll save the envelope of this type with this entity id
            return true;
        }

        String messageRole = (String) exchange.get(DispatchClientDefaultProvider.MESSAGE_ROLE);
        String ebmsMessageId = (String) exchange.get(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY);

        // When responding to a pull request without a user message, it is normal to not save the envelope;
        // here we identify if this is the case and don't log a warning in this legitimate situation
        Ebms3Messaging ebms3Messaging = (Ebms3Messaging) exchange.get(MessageConstants.EMBS3_MESSAGING_OBJECT);
        if (ebms3Messaging != null && ebms3Messaging.getSignalMessage() != null && ebms3Messaging.getSignalMessage().getPullRequest() != null) {
            LOG.debug("Outgoing empty signal envelope in response to pull request [{}] will not be saved", ebmsMessageId);
            return false;
        }

        LOG.warn("Skip saving the outgoing message raw xml envelope: message type is [{}]; user message entity id: [{}]; ebms message id: [{}]; message role: [{}]; ebms3 messaging: [{}]",
                messageType, messageEntityId, ebmsMessageId, messageRole,
                ebms3Messaging == null ? null : ebms3Messaging.getUserMessage() != null ? ebms3Messaging.getUserMessage() : ebms3Messaging.getSignalMessage());
        return false;
    }

}
