package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.retention.MessageRetentionService;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
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
    private UserMessageLogDefaultService userMessageLogService;

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

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    MessageRetentionService messageRetentionService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReliabilityInNewTransaction(String messageId, Messaging messaging, UserMessageLog userMessageLog, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration, final MessageAttempt attempt) {
        LOG.debug("Handling reliability in a new transaction");
        handleReliability(messageId, messaging, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleReliability(String messageId, Messaging messaging, UserMessageLog userMessageLog, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration, final MessageAttempt attempt) {
        LOG.debug("Handling reliability");

        final Boolean isTestMessage = userMessageLog.isTestMessage();
        final UserMessage userMessage = messaging.getUserMessage();

        switch (reliabilityCheckSuccessful) {
            case OK:
                responseHandler.saveResponse(responseSoapMessage, messaging, responseResult.getResponseMessaging());

                ResponseHandler.ResponseStatus responseStatus = responseResult.getResponseStatus();
                switch (responseStatus) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(userMessage, userMessageLog);

                        if (userMessage.isUserMessageFragment()) {
                            splitAndJoinService.incrementSentFragments(userMessage.getMessageFragment().getGroupId());
                        }
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(userMessage, userMessageLog);
                        break;
                    default:
                        assert false;
                }
                if (!isTestMessage) {
                    backendNotificationService.notifyOfSendSuccess(userMessageLog);
                }
                userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
                messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);
                LOG.businessInfo(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_SUCCESS : DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS,
                        userMessage.getFromFirstPartyId(), userMessage.getToFirstPartyId());

                userMessageLogDao.update(userMessageLog);
                break;
            case WAITING_FOR_CALLBACK:
                updateRetryLoggingService.updateWaitingReceiptMessageRetryLogging(messageId, legConfiguration);
                break;
            case SEND_FAIL:
                updateRetryLoggingService.updatePushedMessageRetryLogging(messageId, legConfiguration, attempt);
                break;
            case ABORT:
                updateRetryLoggingService.messageFailedInANewTransaction(userMessage, userMessageLog, attempt);

                if (userMessage.isUserMessageFragment()) {
                    userMessageService.scheduleSplitAndJoinSendFailed(userMessage.getMessageFragment().getGroupId(), String.format("Message fragment [%s] has failed to be sent", messageId));
                }
                break;
        }
        //call ui replication sync service
        uiReplicationSignalService.messageChange(messageId);

        LOG.debug("Finished handling reliability");
    }


}
