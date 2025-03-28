package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.pull.IncomingPullReceiptHandler;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityDTOBuilder;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.interceptor.Fault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;

import static eu.domibus.logging.DomibusMessageCode.BUS_MESSAGE_RECEIPT_RECEIVED_FAILED;

/**
 * Handles the incoming AS4 receipts for User Messages (that were sent with either PUSH or PULL).
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingUserMessageReceiptHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingUserMessageReceiptHandler.class);

    protected Reliability sourceMessageReliability;

    protected final ReliabilityService reliabilityService;
    private final ReliabilityChecker reliabilityChecker;
    private final UserMessageLogDao userMessageLogDao;
    private final EbMS3MessageBuilder messageBuilder;
    private final ResponseHandler responseHandler;
    private final PModeProvider pModeProvider;
    private final UserMessageDao userMessageDao;
    protected final MessageUtil messageUtil;
    protected final SoapUtil soapUtil;
    protected Ebms3Converter ebms3Converter;
    protected PartInfoDao partInfoDao;
    protected final IncomingPullReceiptHandler incomingPullReceiptHandler;

    public IncomingUserMessageReceiptHandler(ReliabilityService reliabilityService,
                                             ReliabilityChecker reliabilityChecker,
                                             UserMessageLogDao userMessageLogDao,
                                             EbMS3MessageBuilder messageBuilder,
                                             ResponseHandler responseHandler,
                                             PModeProvider pModeProvider,
                                             UserMessageDao userMessageDao,
                                             MessageUtil messageUtil,
                                             SoapUtil soapUtil,
                                             Ebms3Converter ebms3Converter,
                                             PartInfoDao partInfoDao,
                                             IncomingPullReceiptHandler incomingPullReceiptHandler) {
        this.reliabilityService = reliabilityService;
        this.reliabilityChecker = reliabilityChecker;
        this.userMessageLogDao = userMessageLogDao;
        this.messageBuilder = messageBuilder;
        this.responseHandler = responseHandler;
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.messageUtil = messageUtil;
        this.soapUtil = soapUtil;
        this.ebms3Converter = ebms3Converter;
        this.partInfoDao = partInfoDao;
        this.incomingPullReceiptHandler = incomingPullReceiptHandler;
    }

    @Transactional
    @Override
    @Timer(clazz = IncomingUserMessageReceiptHandler.class, value = "incoming_user_message_receipt")
    @Counter(clazz = IncomingUserMessageReceiptHandler.class, value = "incoming_user_message_receipt")
    public SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging ebms3Messaging) {
        LOG.debug("Processing UserMessage receipt");

        String refToMessageId = ebms3Messaging.getSignalMessage().getMessageInfo().getRefToMessageId();

        return handleIncomingReceipt(request, refToMessageId);
    }

    protected SOAPMessage handleIncomingReceipt(SOAPMessage request, String messageId) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID, String.valueOf(userMessageLog.getEntityId()));

        if (userMessageLog.getProcessingType() == ProcessingType.PULL) {
            return incomingPullReceiptHandler.handlePullRequestReceipt(request, messageId, userMessageLog);
        } else {
            return handlePushUserMessageReceipt(request, messageId, userMessageLog);
        }
    }

    protected SOAPMessage handlePushUserMessageReceipt(SOAPMessage request, String messageId, final UserMessageLog userMessageLog) {
        if (MessageStatus.ACKNOWLEDGED == userMessageLog.getMessageStatus()) {
            LOG.error("Received a UserMessage receipt for an already acknowledged message with status [{}]", userMessageLog.getMessageStatus());
            return messageBuilder.getSoapMessage(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0302)
                    .message(String.format("UserMessage with ID [%s] is already acknowledged", messageId))
                    .refToMessageId(messageId)
                    .build());
        }
        ReliabilityDTOBuilder reliabilityDTOBuilder = ReliabilityDTOBuilder.builder().userMessageLog(userMessageLog);

        ReliabilityChecker.CheckResult reliabilityCheckResult = ReliabilityChecker.CheckResult.ABORT;
        ResponseResult responseResult = null;
        LegConfiguration legConfiguration = null;
        UserMessage sentUserMessage = null;
        try {
            sentUserMessage = userMessageDao.findByEntityId(userMessageLog.getEntityId());
            LOG.putMDC(DomibusLogger.MDC_FROM, sentUserMessage.getPartyInfo().getFromParty());
            LOG.putMDC(DomibusLogger.MDC_TO, sentUserMessage.getPartyInfo().getToParty());
            LOG.putMDC(DomibusLogger.MDC_CONVERSATION_ID, sentUserMessage.getConversationId());
            String pModeKey = pModeProvider.findUserMessageExchangeContext(sentUserMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : {}", pModeKey);

            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            SOAPMessage soapMessage = getSoapMessage(legConfiguration, sentUserMessage);
            responseResult = responseHandler.verifyResponse(request, messageId);

            reliabilityCheckResult = reliabilityChecker.check(soapMessage, request, responseResult, getSourceMessageReliability());
        } catch (final SOAPFaultException soapFEx) {
            LOG.error("A SOAP fault occurred when handling receipt for message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), sentUserMessage);
            }
            reliabilityDTOBuilder.throwable(soapFEx);
        } catch (final EbMS3Exception e) {
            LOG.error("EbMS3 exception occurred when handling receipt for message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, sentUserMessage);
            reliabilityDTOBuilder.throwable(e);
        } finally {

            reliabilityDTOBuilder
                    .userMessage(sentUserMessage)
                    .reliabilityCheckStatus(reliabilityCheckResult)
                    .responseSoapMessage(request)
                    .responseResult(responseResult)
                    .legConfiguration(legConfiguration);

            reliabilityService.handleReliability(reliabilityDTOBuilder.build());
            if (ReliabilityChecker.CheckResult.OK == reliabilityCheckResult) {
                final Boolean isTestMessage = sentUserMessage.isTestMessage();
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIPT_RECEIVED_SUCCESS, ProcessingType.PUSH);
                LOG.businessInfo(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_SUCCESS : DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS,
                        sentUserMessage.getPartyInfo().getFromParty(), sentUserMessage.getPartyInfo().getToParty());
            } else {
                LOG.businessError(BUS_MESSAGE_RECEIPT_RECEIVED_FAILED, null, ProcessingType.PUSH);
            }
        }
        return null;
    }

    protected SOAPMessage getSoapMessage(LegConfiguration legConfiguration, UserMessage userMessage) throws EbMS3Exception {
        final List<PartInfo> partInfoList = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
        return messageBuilder.buildSOAPMessage(userMessage, partInfoList, legConfiguration);
    }

    protected Reliability getSourceMessageReliability() {
        if (sourceMessageReliability == null) {
            sourceMessageReliability = new Reliability();
            sourceMessageReliability.setNonRepudiation(false);
            sourceMessageReliability.setReplyPattern(ReplyPattern.RESPONSE);
        }
        return sourceMessageReliability;
    }

}
