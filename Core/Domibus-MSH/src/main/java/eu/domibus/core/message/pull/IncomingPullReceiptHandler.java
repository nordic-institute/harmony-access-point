package eu.domibus.core.message.pull;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
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
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PULL_RECEIPT_RELIABILITY_RETRY;
import static eu.domibus.logging.DomibusMessageCode.BUS_MESSAGE_RECEIPT_RECEIVED_FAILED;
import static eu.domibus.logging.DomibusMessageCode.BUS_MESSAGE_RECEIPT_RECEIVED_SUCCESS;

/**
 * Handles the incoming AS4 pull receipt
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingPullReceiptHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullReceiptHandler.class);

    private final MessageExchangeService messageExchangeService;
    private final ReliabilityChecker reliabilityChecker;
    private final ReliabilityMatcher pullReceiptMatcher;
    private final PullMessageService pullMessageService;
    private final EbMS3MessageBuilder messageBuilder;
    private final ResponseHandler responseHandler;
    private final PModeProvider pModeProvider;
    protected final UserMessageDao userMessageDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;
    protected final PartInfoDao partInfoDao;
    protected final DomibusPropertyProvider domibusPropertyProvider;

    public IncomingPullReceiptHandler(
            MessageExchangeService messageExchangeService,
            ReliabilityChecker reliabilityChecker,
            ReliabilityMatcher pullReceiptMatcher,
            PullMessageService pullMessageService,
            EbMS3MessageBuilder messageBuilder,
            ResponseHandler responseHandler,
            PModeProvider pModeProvider,
            UserMessageDao userMessageDao,
            MessageUtil messageUtil,
            SoapUtil soapUtil,
            PartInfoDao partInfoDao,
            DomibusPropertyProvider domibusPropertyProvider) {
        this.messageExchangeService = messageExchangeService;
        this.reliabilityChecker = reliabilityChecker;
        this.pullReceiptMatcher = pullReceiptMatcher;
        this.pullMessageService = pullMessageService;
        this.responseHandler = responseHandler;
        this.messageBuilder = messageBuilder;
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
        this.partInfoDao = partInfoDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public SOAPMessage handlePullRequestReceipt(SOAPMessage request, String messageId, final UserMessageLog userMessageLog) {
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.PULL_FAILED;
        ResponseHandler.ResponseStatus isOk = null;
        LegConfiguration legConfiguration = null;
        UserMessage userMessage = userMessageDao.findByEntityId(userMessageLog.getEntityId());
        LOG.putMDC(DomibusLogger.MDC_FROM, userMessage.getPartyInfo().getFromParty());
        LOG.putMDC(DomibusLogger.MDC_TO, userMessage.getPartyInfo().getToParty());
        LOG.putMDC(DomibusLogger.MDC_CONVERSATION_ID, userMessage.getConversationId());
        LOG.debug("Handle PULL receipt [{}]", userMessage);
        if (MessageStatus.WAITING_FOR_RECEIPT != userMessageLog.getMessageStatus()) {
            LOG.error("[PULL_RECEIPT]:Message:[{}] receipt a pull acknowledgement but its status is [{}]", messageId, userMessageLog.getMessageStatus());
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("No message in waiting for callback state found for receipt referring to :[%s]", messageId))
                    .refToMessageId(messageId)
                    .build());
        }
        LOG.debug("[handlePullRequestReceipt]:Message:[{}] checking lock ", messageId);

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
        ResponseResult responseResult = null;
        Throwable throwable = null;
        try {
            String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true).getPmodeKey();
            LOG.debug("PMode key found : [{}]", pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);
            SOAPMessage soapMessage = getSoapMessage(messageId, legConfiguration, userMessage);
            responseResult = responseHandler.verifyResponse(request, messageId);
            isOk = responseResult.getResponseStatus();

            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, request, responseResult, legConfiguration, pullReceiptMatcher);
        } catch (final SOAPFaultException soapFEx) {
            throwable = soapFEx;
            LOG.error("A SOAP fault occurred when handling pull receipt for message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), userMessage);
            }
        } catch (final EbMS3Exception e) {
            throwable = e;
            LOG.error("EbMS3 exception occurred when handling pull receipt for message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, userMessage);
        } catch (ReliabilityException r) {
            throwable = r;
            LOG.error("Reliability exception occurred when handling pull receipt for message with ID [{}]", messageId, r);
        } catch (Throwable tr) {
            throwable = tr;
        } finally {
            final PullRequestResult pullRequestResult = pullMessageService.updatePullMessageAfterReceipt(reliabilityCheckSuccessful, isOk, responseResult, request, userMessageLog, legConfiguration, userMessage);
            pullMessageService.releaseLockAfterReceipt(pullRequestResult);
        }
        if ((isOk != ResponseHandler.ResponseStatus.OK && isOk != ResponseHandler.ResponseStatus.WARNING) ||
                (reliabilityCheckSuccessful != ReliabilityChecker.CheckResult.OK)) {
            LOG.businessError(BUS_MESSAGE_RECEIPT_RECEIVED_FAILED, throwable, ProcessingType.PULL);
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("There was an error processing the receipt for pulled message:[%s].", messageId))
                    .refToMessageId(messageId)
                    .build());
        } else {
            LOG.businessInfo(BUS_MESSAGE_RECEIPT_RECEIVED_SUCCESS, ProcessingType.PULL);
        }

        // when the pull receipt is valid, no response is expected back
        return null;
    }

    protected SOAPMessage getSoapMessage(String messageId, LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        SOAPMessage soapMessage;
        if (pullReceiptMatcher.matchReliableReceipt(legConfiguration.getReliability()) && legConfiguration.getReliability().isNonRepudiation()) {
            RawEnvelopeDto rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageEntityId(userMessage.getEntityId());
            if (rawEnvelopeDto == null) {
                final int retryInMs = domibusPropertyProvider.getIntegerProperty(DOMIBUS_PULL_RECEIPT_RELIABILITY_RETRY);
                if (retryInMs <= 0) {
                    LOG.warn("User message raw envelope not found for [{}] message with id [{}] and message entity id [{}]. No retry will be attempted", userMessage.getMshRole().getRole(), messageId, userMessage.getEntityId());
                    throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "There should always be a raw message for " + messageId);
                }

                LOG.warn("User message raw envelope not found for [{}] message with id [{}] and message entity id [{}]. A retry will be attempted after [{}] ms", userMessage.getMshRole().getRole(), messageId, userMessage.getEntityId(), retryInMs);
                try {
                    Thread.sleep(retryInMs);
                } catch (InterruptedException e) {
                    LOG.warn("Thread interrupted while waiting for retry", e);
                    throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "There should always be a raw message for " + messageId);
                }

                rawEnvelopeDto = messageExchangeService.findPulledMessageRawXmlByMessageEntityId(userMessage.getEntityId());
                if (rawEnvelopeDto == null) {
                    LOG.warn("User message raw envelope not found for [{}] message with id [{}] and message entity id [{}]", userMessage.getMshRole().getRole(), messageId, userMessage.getEntityId());
                    throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "There should always be a raw message for " + messageId);
                }

                LOG.info("User message raw envelope found on second try for [{}] message with id [{}] and message entity id [{}]", userMessage.getMshRole().getRole(), messageId, userMessage.getEntityId());
            }

            try {
                final String rawXml = rawEnvelopeDto.getRawXmlMessage();
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
