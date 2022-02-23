package eu.domibus.core.message.pull;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

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
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

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
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(messageId);
        }
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(userMessageLog.getEntityId());
        sendFailed(userMessageLog, messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void sendFailed(final UserMessageLog userMessageLog, String messageId) {
        if (userMessageLog == null) {
            LOG.warn("Could not mark message as failed: userMessageLog is null");
            return;
        }

        LOG.debug("Setting [{}] message as failed", messageId);
        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            LOG.debug("Could not set [{}] message as failed: could not find userMessage", messageId);
            return;
        }

        updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void reset(final UserMessageLog userMessageLog, String messageId) {
        final MessageStatusEntity readyToPull = messageStatusDao.findOrCreate(MessageStatus.READY_TO_PULL);
        LOG.debug("Change message:[{}] with state:[{}] to state:[{}].", messageId, userMessageLog.getMessageStatus(), readyToPull);
        userMessageLog.setMessageStatus(readyToPull);
        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageChange(messageId);
        backendNotificationService.notifyOfMessageStatusChange(messageId, userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));
    }


}
