package eu.domibus.core.message.reliability;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
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
    private MessageGroupDao messageGroupDao;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

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
    MessageRetentionDefaultService messageRetentionService;


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReliabilityInNewTransaction(UserMessage userMessage, UserMessageLog userMessageLog, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration, final MessageAttempt attempt) {
        LOG.debug("Handling reliability in a new transaction");
        handleReliability(userMessage, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handleReliability(UserMessage userMessage, UserMessageLog userMessageLog, final ReliabilityChecker.CheckResult reliabilityCheckSuccessful, SOAPMessage responseSoapMessage, final ResponseResult responseResult, final LegConfiguration legConfiguration, final MessageAttempt attempt) {
        LOG.debug("Handling reliability");

        final Boolean isTestMessage = userMessage.isTestMessage();

        switch (reliabilityCheckSuccessful) {
            case OK:
                responseHandler.saveResponse(responseSoapMessage, userMessage, responseResult.getResponseMessaging());

                ResponseHandler.ResponseStatus responseStatus = responseResult.getResponseStatus();
                switch (responseStatus) {
                    case OK:
                        userMessageLogService.setMessageAsAcknowledged(userMessage, userMessageLog);

                        if (userMessage.isMessageFragment()) {
                            MessageGroupEntity messageGroupEntity = messageGroupDao.findByUserMessageEntityId(userMessage.getEntityId());
                            splitAndJoinService.incrementSentFragments(messageGroupEntity.getGroupId());
                        }
                        break;
                    case WARNING:
                        userMessageLogService.setMessageAsAckWithWarnings(userMessage, userMessageLog);
                        break;
                    default:
                        assert false;
                }
                if (!isTestMessage) {
                    backendNotificationService.notifyOfSendSuccess(userMessage, userMessageLog);
                }
                userMessageLog.setSendAttempts(userMessageLog.getSendAttempts() + 1);
                messageRetentionService.deletePayloadOnSendSuccess(userMessage, userMessageLog);
                LOG.businessInfo(isTestMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_SUCCESS : DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS,
                        userMessage.getPartyInfo().getFromParty(), userMessage.getPartyInfo().getToParty());

                userMessageLogDao.update(userMessageLog);
                break;
            case WAITING_FOR_CALLBACK:
                updateRetryLoggingService.updateWaitingReceiptMessageRetryLogging(userMessage, legConfiguration);
                break;
            case SEND_FAIL:
                updateRetryLoggingService.updatePushedMessageRetryLogging(userMessage, legConfiguration, attempt);
                break;
            case ABORT:
                updateRetryLoggingService.messageFailedInANewTransaction(userMessage, userMessageLog, attempt);

                if (userMessage.isMessageFragment()) {
                    MessageGroupEntity messageGroupEntity = messageGroupDao.findByUserMessageEntityId(userMessage.getEntityId());
                    userMessageService.scheduleSplitAndJoinSendFailed(messageGroupEntity.getGroupId(), String.format("Message fragment [%s] has failed to be sent", userMessage.getMessageId()));
                }
                break;
        }
        //call ui replication sync service
        uiReplicationSignalService.messageChange(userMessage.getMessageId());

        LOG.debug("Finished handling reliability");
    }


}
