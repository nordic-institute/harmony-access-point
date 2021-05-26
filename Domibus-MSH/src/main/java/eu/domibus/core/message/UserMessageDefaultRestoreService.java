package eu.domibus.core.message;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This service class is responsible for the restore of failed messages.
 *
 * @author Soumya
 * @since 4.2.2
 */

@Service
public class UserMessageDefaultRestoreService implements UserMessageRestoreService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultRestoreService.class);

    private MessageExchangeService messageExchangeService;

    private BackendNotificationService backendNotificationService;

    private UIReplicationSignalService uiReplicationSignalService;

    private UserMessageLogDao userMessageLogDao;

    private MessagingDao messagingDao;

    private PModeProvider pModeProvider;

    private PullMessageService pullMessageService;

    private PModeService pModeService;

    private PModeServiceHelper pModeServiceHelper;

    private UserMessageDefaultService userMessageService;

    public UserMessageDefaultRestoreService(MessageExchangeService messageExchangeService, BackendNotificationService backendNotificationService, UIReplicationSignalService uiReplicationSignalService, UserMessageLogDao userMessageLogDao, MessagingDao messagingDao, PModeProvider pModeProvider, PullMessageService pullMessageService, PModeService pModeService, PModeServiceHelper pModeServiceHelper, UserMessageDefaultService userMessageService) {
        this.messageExchangeService = messageExchangeService;
        this.backendNotificationService = backendNotificationService;
        this.uiReplicationSignalService = uiReplicationSignalService;
        this.userMessageLogDao = userMessageLogDao;
        this.messagingDao = messagingDao;
        this.pModeProvider = pModeProvider;
        this.pullMessageService = pullMessageService;
        this.pModeService = pModeService;
        this.pModeServiceHelper = pModeServiceHelper;
        this.userMessageService = userMessageService;
    }


    @Transactional
    @Override
    public void restoreFailedMessage(String messageId) {
        LOG.info("Restoring message [{}]", messageId);
        final UserMessageLog userMessageLog = userMessageService.getFailedMessage(messageId);

        if (MessageStatus.DELETED == userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "]. Message status is [" + MessageStatus.DELETED + "]");
        }

        restoreMessage(messageId, userMessageLog);
    }

    @Transactional
    @Override
    public void restoreSendEnqueuedMessage(String messageId) {
        LOG.info("Restoring SendEnqueued message [{}]", messageId);
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);

        if (userMessageLog == null) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "]. Message doesn't exist");
        }
        if (MessageStatus.SEND_ENQUEUED != userMessageLog.getMessageStatus()) {
            throw new UserMessageException(DomibusCoreErrorCode.DOM_001, "Could not restore message [" + messageId + "] status is not [" + MessageStatus.SEND_ENQUEUED + "]");
        }
        restoreMessage(messageId, userMessageLog);
    }

    protected void restoreMessage(String messageId, UserMessageLog userMessageLog) {
        final MessageStatus newMessageStatus = messageExchangeService.retrieveMessageRestoreStatus(messageId);
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, newMessageStatus, new Timestamp(System.currentTimeMillis()));
        userMessageLog.setMessageStatus(newMessageStatus);
        final Date currentDate = new Date();
        userMessageLog.setRestored(currentDate);
        userMessageLog.setFailed(null);
        userMessageLog.setNextAttempt(currentDate);

        Integer newMaxAttempts = computeNewMaxAttempts(userMessageLog, messageId);
        LOG.debug("Increasing the max attempts for message [{}] from [{}] to [{}]", messageId, userMessageLog.getSendAttemptsMax(), newMaxAttempts);
        userMessageLog.setSendAttemptsMax(newMaxAttempts);

        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (MessageStatus.READY_TO_PULL != newMessageStatus) {
            userMessageService.scheduleSending(userMessage, userMessageLog);
        } else {
            try {
                MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
                Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
                LOG.debug("[restoreFailedMessage]:Message:[{}] add lock", userMessageLog.getMessageId());
                pullMessageService.addPullMessageLock(userMessage, userMessageLog);
            } catch (EbMS3Exception ebms3Ex) {
                LOG.error("Error restoring user message to ready to pull[" + userMessage.getMessageInfo().getMessageId() + "]", ebms3Ex);
            }
        }
    }

    protected Integer getMaxAttemptsConfiguration(final String messageId) {
        final LegConfiguration legConfiguration = pModeService.getLegConfiguration(messageId);
        Integer result = 1;
        if (legConfiguration == null) {
            LOG.warn("Could not get the leg configuration for message [{}]. Using the default maxAttempts configuration [{}]", messageId, result);
        } else {
            result = pModeServiceHelper.getMaxAttempts(legConfiguration);
        }
        return result;
    }

    protected Integer computeNewMaxAttempts(final UserMessageLog userMessageLog, final String messageId) {
        Integer maxAttemptsConfiguration = getMaxAttemptsConfiguration(messageId);
        // always increase maxAttempts (even when not reached by sendAttempts)
        return userMessageLog.getSendAttemptsMax() + maxAttemptsConfiguration + 1; // max retries plus initial reattempt
    }
}
