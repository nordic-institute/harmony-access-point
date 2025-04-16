package eu.domibus.core.message.pull;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static eu.domibus.api.model.ProcessingType.PULL;

/**
 * @author Thomas Dussart
 * @since 3.3.3
 * <p>
 * {@inheritDoc}
 */
@Service
public class PullMessageStateServiceImpl implements PullMessageStateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullMessageStateServiceImpl.class);

    @Autowired
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Autowired
    protected UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UserMessageDao userMessageDao;

    @Autowired
    protected MessageStatusDao messageStatusDao;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirePullMessage(final String messageId) {
        LOG.debug("Message:[{}] expired.", messageId);
        final UserMessageLog userMessageLog = userMessageLogService.findByMessageId(messageId, MSHRole.SENDING);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getEntityId());
        sendFailed(userMessageLog, messageId);
    }

    @Override
    @Transactional
    public void sendFailed(final UserMessageLog userMessageLog, UserMessage userMessage) {
        if (userMessageLog == null) {
            LOG.warn("Could not mark message as failed: userMessageLog is null");
            return;
        }
        if (userMessage == null) {
            LOG.debug("Could not set message as failed because no userMessage was found");
            return;
        }
        LOG.debug("Setting [{}] message as failed", userMessage.getMessageId());
        updateRetryLoggingService.messageFailed(userMessage, userMessageLog, PULL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @MDCKey(value = {DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_FROM, DomibusLogger.MDC_TO, DomibusLogger.MDC_CONVERSATION_ID})
    public void sendFailed(final UserMessageLog userMessageLog, String messageId) {
        if (userMessageLog == null) {
            LOG.warn("Could not mark message as failed: userMessageLog is null");
            return;
        }

        LOG.debug("Setting [{}] message as failed", messageId);
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, MSHRole.SENDING);

        if (userMessage == null) {
            LOG.debug("Could not set [{}] message as failed: could not find userMessage", messageId);
            return;
        }

        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, userMessage.getMessageId());
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ROLE, userMessage.getMshRole().getRole().name());
        LOG.putMDC(DomibusLogger.MDC_FROM, userMessage.getPartyInfo().getFromParty());
        LOG.putMDC(DomibusLogger.MDC_TO, userMessage.getPartyInfo().getToParty());
        LOG.putMDC(DomibusLogger.MDC_CONVERSATION_ID, userMessage.getConversationId());
        updateRetryLoggingService.messageFailed(userMessage, userMessageLog, PULL);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void reset(final UserMessageLog userMessageLog, UserMessage userMessage) {
        userMessageLogService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.READY_TO_PULL);
        userMessageLogService.update(userMessageLog);
    }


}
