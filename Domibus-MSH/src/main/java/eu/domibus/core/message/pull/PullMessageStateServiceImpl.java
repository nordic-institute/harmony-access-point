package eu.domibus.core.message.pull;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
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
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected MessagingDao messagingDao;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirePullMessage(final String messageId) {
        LOG.debug("Message:[{}] expired.", messageId);
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
        sendFailed(userMessageLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void sendFailed(final UserMessageLog userMessageLog) {
        if (userMessageLog == null) {
            LOG.warn("Could not mark message as failed: userMessageLog is null");
            return;
        }

        LOG.debug("Setting [{}] message as failed", userMessageLog.getMessageId());
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(userMessageLog.getMessageId());
        if (userMessage == null) {
            LOG.debug("Could not set [{}] message as failed: could not find userMessage", userMessageLog.getMessageId());
            return;
        }

        updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void reset(final UserMessageLog userMessageLog) {
        final MessageStatus readyToPull = MessageStatus.READY_TO_PULL;
        LOG.debug("Change message:[{}] with state:[{}] to state:[{}].", userMessageLog.getMessageId(), userMessageLog.getMessageStatus(), readyToPull);
        userMessageLog.setMessageStatus(readyToPull);
        userMessageLogDao.update(userMessageLog);
        uiReplicationSignalService.messageChange(userMessageLog.getMessageId());
        backendNotificationService.notifyOfMessageStatusChange(userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(System.currentTimeMillis()));
    }


}
