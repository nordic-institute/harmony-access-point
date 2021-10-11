package eu.domibus.core.message.pull;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.*;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    protected UserMessageDao userMessageDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;
    protected PartInfoDao partInfoDao;

    public IncomingPullReceiptHandler(
            MessageExchangeService messageExchangeService,
            ReliabilityChecker reliabilityChecker,
            ReliabilityMatcher pullReceiptMatcher,
            PullMessageService pullMessageService,
            UserMessageLogDao userMessageLogDao,
            EbMS3MessageBuilder messageBuilder,
            ResponseHandler responseHandler,
            PModeProvider pModeProvider,
            UserMessageDao userMessageDao,
            MessageUtil messageUtil,
            SoapUtil soapUtil,
            PartInfoDao partInfoDao) {
        this.messageExchangeService = messageExchangeService;
        this.reliabilityChecker = reliabilityChecker;
        this.pullReceiptMatcher = pullReceiptMatcher;
        this.pullMessageService = pullMessageService;
        this.userMessageLogDao = userMessageLogDao;
        this.responseHandler = responseHandler;
        this.messageBuilder = messageBuilder;
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
        this.partInfoDao = partInfoDao;
    }

    @Override
    @Timer(clazz = IncomingPullReceiptHandler.class,value = "incoming_pull_request_receipt")
    @Counter(clazz = IncomingPullReceiptHandler.class,value = "incoming_pull_request_receipt")
    public SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging ebms3Messaging) {
        LOG.trace("before pull receipt.");

        String messageId = ebms3Messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
        final SOAPMessage soapMessage = handlePullRequestReceipt(request, messageId);
        LOG.trace("returning pull receipt.");
        return soapMessage;
    }

    protected SOAPMessage handlePullRequestReceipt(SOAPMessage request, String messageId) {

        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.PULL_FAILED;
        ResponseHandler.ResponseStatus isOk = null;
        LegConfiguration legConfiguration = null;
        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        if (MessageStatus.WAITING_FOR_RECEIPT != userMessageLog.getMessageStatus()) {
            LOG.error("[PULL_RECEIPT]:Message:[{}] receipt a pull acknowledgement but its status is [{}]", messageId, userMessageLog.getMessageStatus());
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("No message in waiting for callback state found for receipt referring to :[%s]", messageId))
                    .refToMessageId(messageId)
                    .build());
        }
        LOG.debug("[handlePullRequestReceipt]:Message:[{}] delete lock ", messageId);

        final MessagingLock lock = pullMessageService.getLock(messageId);
        if (lock == null || MessageState.WAITING != lock.getMessageState()) {
            LOG.trace("Message[{}] could not acquire lock", messageId);
            LOG.error("[PULL_RECEIPT]:Message:[{}] time to receipt a pull acknowledgement has expired.", messageId);
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("Time to receipt a pull acknowledgement for message:[%s] has expired", messageId))
                    .refToMessageId(messageId)
                    .build());
        }

        try {
            String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true, null).getPmodeKey();
            LOG.debug("PMode key found : [{}]", pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
            SOAPMessage soapMessage = getSoapMessage(messageId, legConfiguration, userMessage);
            final ResponseResult responseResult = responseHandler.verifyResponse(request, messageId);
            isOk = responseResult.getResponseStatus();

            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, responseResult, legConfiguration, pullReceiptMatcher);
        } catch (final SOAPFaultException soapFEx) {
            LOG.error("A SOAP fault occurred when handling pull receipt for message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), userMessage);
            }
        } catch (final EbMS3Exception e) {
            LOG.error("EbMS3 exception occurred when handling pull receipt for message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, userMessage);
        } catch (ReliabilityException r) {
            LOG.error("Reliability exception occurred when handling pull receipt for message with ID [{}]", messageId, r);
        } finally {
            final PullRequestResult pullRequestResult = pullMessageService.updatePullMessageAfterReceipt(reliabilityCheckSuccessful, isOk, userMessageLog, legConfiguration, userMessage);
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
        }
        if((isOk != ResponseHandler.ResponseStatus.OK && isOk != ResponseHandler.ResponseStatus.WARNING) ||
                (reliabilityCheckSuccessful != ReliabilityChecker.CheckResult.OK)) {
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("There was an error processing the receipt for pulled message:[%s].", messageId))
                    .refToMessageId(messageId)
                    .build());
        }

        // when the pull receipt is valid, no response is expected back
        return null;
    }

    protected SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        SOAPMessage soapMessage;
        if (pullReceiptMatcher.matchReliableReceipt(legConfiguration.getReliability()) && legConfiguration.getReliability().isNonRepudiation()) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageId(messageId);
            try {
                final byte[] rawMessage = rawEnvelopeDto.getRawMessage();
                final String rawXml = new String(rawMessage, StandardCharsets.UTF_8);
                soapMessage = soapUtil.createSOAPMessage(rawXml);
            } catch (ParserConfigurationException | SOAPException | SAXException | IOException e) {
                throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "Raw message found in db but impossible to restore it");
            }
        } else {
            final List<PartInfo> partInfoList = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
            soapMessage = messageBuilder.buildSOAPMessage(userMessage, partInfoList, legConfiguration);
        }
        return soapMessage;
    }

}
