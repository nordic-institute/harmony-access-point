package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
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
        MessageType messageType = (MessageType) message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);

        String ebmsMessageId = (String) message.getExchange().get(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY);
        String messageEntityIdValue = (String) message.getExchange().get(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY);
        Long messageEntityId = StringUtils.isBlank(messageEntityIdValue) ? null : Long.valueOf(messageEntityIdValue);
        boolean duplicateMessage = BooleanUtils.toBoolean(userMessageContextKeyProvider.getKeyFromTheCurrentMessage(UserMessage.USER_MESSAGE_DUPLICATE_KEY));

        String outgoingUserMessageId = (String) message.getExchange().get(DispatchClientDefaultProvider.MESSAGE_ID);
        String messageRole = (String) message.getExchange().get(DispatchClientDefaultProvider.MESSAGE_ROLE);
        if (messageType == null || messageEntityId == null || duplicateMessage) {
            LOG.debug("Skip saving the outgoing message raw xml envelope: message type is [{}]; user message entity id: [{}]; duplicateMessage: [{}]", messageType, messageEntityId, duplicateMessage);
            return;
        }
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
            }
        } catch (TransformerException e) {
            throw new WebServiceException(new IllegalArgumentException(e));
        } catch (Exception e) { //saving the raw envelope should not prevent the successful exchange of messages
            LOG.error("Could not save outgoing message raw envelope for message entity id [{}]", messageEntityIdValue, e);
        }

    }
}
