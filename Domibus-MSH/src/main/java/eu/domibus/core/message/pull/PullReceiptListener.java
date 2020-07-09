package eu.domibus.core.message.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.ext.domain.metrics.Counter;
import eu.domibus.ext.domain.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;
import java.util.List;

import static eu.domibus.ext.domain.metrics.MetricNames.OUTGOING_PULL_RECEIPT;

/**
 * @author idragusa
 * @since 4.1
 */
@Service(value = "pullReceiptListener")
public class PullReceiptListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullReceiptListener.class);

    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    protected PullReceiptSender pullReceiptSender;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    UserMessageHandlerService userMessageHandlerService;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    UserMessageService userMessageService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(clazz = PullReceiptListener.class,value ="OUTGOING_PULL_RECEIPT")
    @Counter(clazz = PullReceiptListener.class,value ="OUTGOING_PULL_RECEIPT")
    public void onMessage(final Message message) {
        try {
            LOG.clearCustomKeys();

            String domainCode = null;
            try {
                domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            } catch (final JMSException e) {
                LOG.error("Error processing JMS message", e);
            }
            if (StringUtils.isBlank(domainCode)) {
                LOG.error("Domain is empty: could not send message");
                return;
            }
            domainContextProvider.setCurrentDomain(domainCode);
            final String refToMessageId = message.getStringProperty(UserMessageService.PULL_RECEIPT_REF_TO_MESSAGE_ID);
            final String pModeKey = message.getStringProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
            LOG.info("Sending pull receipt for pulled UserMessage [{}], domain [{}].", refToMessageId, domainCode);
            LOG.debug("pModekey is [{}]", pModeKey);
            final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            final Policy policy = policyService.getPolicy(legConfiguration);
            List<SignalMessage> signalMessages = signalMessageDao.findSignalMessagesByRefMessageId(refToMessageId);

            int retryCount = 0;
            try {
                if (message.propertyExists(MessageConstants.RETRY_COUNT)) {
                    retryCount = message.getIntProperty(MessageConstants.RETRY_COUNT);
                }
            } catch (final NumberFormatException nfe) {
                LOG.trace("Error getting message properties", nfe);
                //This is ok, no delay has been set
            } catch (final JMSException e) {
                LOG.error("Error processing JMS message", e);
            }

            if (CollectionUtils.isEmpty(signalMessages)) {
                if (retryCount < MAX_RETRY_COUNT) {
                    userMessageService.scheduleSendingPullReceipt(refToMessageId, pModeKey, retryCount + 1);
                    LOG.warn("Pull receipt not found, retry count is [{}] -> reschedule sending", retryCount);
                    return;
                }
                LOG.warn("Pull receipt for [{}] not found for [{}] times and will not be sent", refToMessageId, retryCount);
                return;
            }

            for (SignalMessage signalMessage : signalMessages) {
                if (signalMessage.getReceipt() != null) { // we have a receipt (it can also be a signal pull request for which we do nothing)
                    if (signalMessage.getReceipt().getAny().size() == 1) {
                        if (userMessageHandlerService.checkSelfSending(pModeKey)) {
                            removeSelfSendingPrefix(signalMessage);
                        }
                        SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(signalMessage, legConfiguration);
                        pullReceiptSender.sendReceipt(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey, refToMessageId, domainCode);
                    } else {
                        LOG.warn("Could not send pull receipt for message [{}]. Invalid receipt(<any>) content size in SignalMessage.", refToMessageId);
                        return;
                    }
                }
            }
        } catch (final JMSException | EbMS3Exception e) {
            LOG.error("Error processing JMS message", e);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error processing JMS message", e);
        }

        LOG.trace("[PullReceiptListener] ~~~ The end of onMessage ~~~");
    }

    protected void removeSelfSendingPrefix(SignalMessage signalMessage) {
        if (signalMessage == null || signalMessage.getMessageInfo() == null) {
            return;
        }
        String messageId = removePrefix(signalMessage.getMessageInfo().getMessageId(), UserMessageHandlerService.SELF_SENDING_SUFFIX);
        String refToMessageId = removePrefix(signalMessage.getMessageInfo().getRefToMessageId(), UserMessageHandlerService.SELF_SENDING_SUFFIX);

        signalMessage.getMessageInfo().setMessageId(messageId);
        signalMessage.getMessageInfo().setRefToMessageId(refToMessageId);
    }

    protected String removePrefix(String messageId, String prefix) {
        String result = messageId;
        if (messageId.endsWith(prefix)) {
            result = messageId.substring(0, messageId.length() - prefix.length());
            LOG.info("Cut prefix from messageId [{}], result is [{}]", messageId, result);
        }
        return result;
    }
}
