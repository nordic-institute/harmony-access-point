package eu.domibus.core.message.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeDto;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.ebms3.common.model.MessageState;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

import static eu.domibus.core.metrics.MetricNames.INCOMING_PULL_REQUEST_RECEIPT;

/**
 * Handles the incoming AS4 pull receipt
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingPullReceiptHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullReceiptHandler.class);

    private final MessageExchangeService messageExchangeService;
    private final ReliabilityChecker reliabilityChecker;
    private final ReliabilityMatcher pullReceiptMatcher;
    private final PullMessageService pullMessageService;
    private final UserMessageLogDao userMessageLogDao;
    private final EbMS3MessageBuilder messageBuilder;
    private final ResponseHandler responseHandler;
    private final PModeProvider pModeProvider;
    private final MessagingDao messagingDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;

    public IncomingPullReceiptHandler(
            MessageExchangeService messageExchangeService,
            ReliabilityChecker reliabilityChecker,
            ReliabilityMatcher pullReceiptMatcher,
            PullMessageService pullMessageService,
            UserMessageLogDao userMessageLogDao,
            EbMS3MessageBuilder messageBuilder,
            ResponseHandler responseHandler,
            PModeProvider pModeProvider,
            MessagingDao messagingDao,
            MessageUtil messageUtil,
            SoapUtil soapUtil) {
        this.messageExchangeService = messageExchangeService;
        this.reliabilityChecker = reliabilityChecker;
        this.pullReceiptMatcher = pullReceiptMatcher;
        this.pullMessageService = pullMessageService;
        this.userMessageLogDao = userMessageLogDao;
        this.responseHandler = responseHandler;
        this.messageBuilder = messageBuilder;
        this.pModeProvider = pModeProvider;
        this.messagingDao = messagingDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
    }

    @Override
    @Timer(INCOMING_PULL_REQUEST_RECEIPT)
    @Counter(INCOMING_PULL_REQUEST_RECEIPT)
    public SOAPMessage processMessage(SOAPMessage request, Messaging messaging) {
        LOG.trace("before pull receipt.");
        final SOAPMessage soapMessage = handlePullRequestReceipt(request, messaging);
        LOG.trace("returning pull receipt.");
        return soapMessage;
    }

    protected SOAPMessage handlePullRequestReceipt(SOAPMessage request, Messaging messaging) {
        String messageId = messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.PULL_FAILED;
        ResponseHandler.ResponseStatus isOk = null;
        LegConfiguration legConfiguration = null;
        UserMessage userMessage = null;
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (MessageStatus.WAITING_FOR_RECEIPT != userMessageLog.getMessageStatus()) {
            LOG.error("[PULL_RECEIPT]:Message:[{}] receipt a pull acknowledgement but its status is [{}]", userMessageLog.getMessageId(), userMessageLog.getMessageStatus());
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, String.format("No message in waiting for callback state found for receipt referring to :[%s]", messageId), messageId, null);
            return messageBuilder.getSoapMessage(ebMS3Exception);
        }
        LOG.debug("[handlePullRequestReceipt]:Message:[{}] delete lock ", messageId);

        final MessagingLock lock = pullMessageService.getLock(messageId);
        if (lock == null || MessageState.WAITING != lock.getMessageState()) {
            LOG.trace("Message[{}] could not acquire lock", messageId);
            LOG.error("[PULL_RECEIPT]:Message:[{}] time to receipt a pull acknowledgement has expired.", messageId);
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, String.format("Time to receipt a pull acknowledgement for message:[%s] has expired", messageId), messageId, null);
            return messageBuilder.getSoapMessage(ebMS3Exception);
        }

        try {
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
            String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
            SOAPMessage soapMessage = getSoapMessage(messageId, legConfiguration, userMessage);
            final ResponseResult responseResult = responseHandler.verifyResponse(request, messageId);
            isOk = responseResult.getResponseStatus();

            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, responseResult, legConfiguration, pullReceiptMatcher);
        } catch (final SOAPFaultException soapFEx) {
            LOG.error("A SOAP fault occurred when handling pull receipt for message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            }
        } catch (final EbMS3Exception e) {
            LOG.error("EbMS3 exception occurred when handling pull receipt for message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, messageId);
        } catch (ReliabilityException r) {
            LOG.error("Reliability exception occurred when handling pull receipt for message with ID [{}]", messageId, r);
        } finally {
            final PullRequestResult pullRequestResult = pullMessageService.updatePullMessageAfterReceipt(reliabilityCheckSuccessful, isOk, userMessageLog, legConfiguration, userMessage);
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
        }
        if((isOk != ResponseHandler.ResponseStatus.OK && isOk != ResponseHandler.ResponseStatus.WARNING) ||
                (reliabilityCheckSuccessful != ReliabilityChecker.CheckResult.OK)) {
            EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0302, String.format("There was an error processing the receipt for pulled message:[%s].", messageId), messageId, null);
            return messageBuilder.getSoapMessage(ebMS3Exception);
        }

        // when the pull receipt is valid, no response is expected back
        return null;
    }

    protected SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        SOAPMessage soapMessage;
        if (pullReceiptMatcher.matchReliableReceipt(legConfiguration.getReliability()) && legConfiguration.getReliability().isNonRepudiation()) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageId(messageId);
            try {
                soapMessage = soapUtil.createSOAPMessage(rawEnvelopeDto.getRawMessage());
            } catch (ParserConfigurationException | SOAPException | SAXException | IOException e) {
                throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "Raw message found in db but impossible to restore it");
            }
        } else {
            soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
        }
        return soapMessage;
    }

}
