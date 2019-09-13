package eu.domibus.common.services.impl;

import eu.domibus.api.message.UserMessageLogService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.ebms3.sender.ResponseHandler;
import eu.domibus.ebms3.sender.ResponseResult;
import eu.domibus.ebms3.sender.UpdateRetryLoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;

/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 3.3
 */

@Service
public class ReliabilityServiceImpl implements ReliabilityService {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ReliabilityServiceImpl.class);

    @Autowired
    private UserMessageLogService userMessageLogService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    private UserMessageHandlerService userMessageHandlerService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected ResponseHandler responseHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReliabilityInNewTransaction(String messageId, UserMessage userMessage, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration) {
        LOG.debug("Handling reliability in a new transaction");
        handleReliability(messageId, userMessage, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleReliability(String messageId, UserMessage userMessage, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration) {
        LOG.debug("Handling reliability");

        LOG.debug("Start changeMessageStatusAndNotify");

        final Boolean isTestMessage = userMessageHandlerService.checkTestMessage(legConfiguration);
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);

        switch (reliabilityCheckSuccessful) {
            case OK:
                responseHandler.saveResponse(responseSoapMessage, responseResult.getResponseMessaging());

                ResponseHandler.ResponseStatus responseStatus = responseResult.getResponseStatus();
                switch (responseStatus) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(messageId);

                        if (userMessage.isUserMessageFragment()) {
                            splitAndJoinService.incrementSentFragments(userMessage.getMessageFragment().getGroupId());
                        }
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(messageId);
                        break;
                    default:
                        assert false;
                }
                if (!isTestMessage) {
                    backendNotificationService.notifyOfSendSuccess(messageId);
                }
                userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
                messagingDao.clearPayloadData(messageId);
                LOG.businessInfo(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_SUCCESS : DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS,
                        userMessage.getFromFirstPartyId(), userMessage.getToFirstPartyId());
                break;
            case WAITING_FOR_CALLBACK:
                updateRetryLoggingService.updateWaitingReceiptMessageRetryLogging(messageId, legConfiguration);
                break;
            case SEND_FAIL:
                updateRetryLoggingService.updatePushedMessageRetryLogging(messageId, legConfiguration);
                break;
            case ABORT:
                updateRetryLoggingService.messageFailedInANewTransaction(userMessage, userMessageLog);

                if (userMessage.isUserMessageFragment()) {
                    userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId(), String.format("Message fragment [%s] has failed to be sent", messageId));
                }
                break;
        }

        LOG.debug("Finished changeMessageStatusAndNotify");
    }
}
