package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.logging.MessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.UserMessageDefaultServiceHelper;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


/**
 * Service dedicate to replicate
 * data in <code>TB_MESSAGE_UI</> table
 * It first reads existing data and then insert it
 *
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0
 */
@Service
public class UIReplicationDataServiceImpl implements UIReplicationDataService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationDataServiceImpl.class);

    static final String LOG_WARN_NO_RECORD_FOUND = "no record found into TB_MESSAGE_UI for messageId=[{}]";

    @Autowired
    private UIMessageDaoImpl uiMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

    @Autowired
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    @Autowired
    private DomainCoreConverter domainConverter;

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageReceived(String messageId, long jmsTimestamp) {
        saveUIMessageFromUserMessageLog(messageId, jmsTimestamp);
    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageSubmitted(String messageId, long jmsTimestamp) {
        LOG.debug("UserMessage=[{}] submitted", messageId);
        saveUIMessageFromUserMessageLog(messageId, jmsTimestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param messageStatus
     * @param jmsTimestamp
     */
    @Override
    public void messageStatusChange(String messageId, MessageStatus messageStatus, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity == null) {
            LOG.warn(LOG_WARN_NO_RECORD_FOUND, messageId);
            return;
        }

        if (entity.getLastModified() == null) {
            LOG.warn("LAST_MODIFIED is null for messageid=[{}]", messageId);
            return;
        }

        if (entity.getLastModified().getTime() <= jmsTimestamp) {
            boolean updateSuccess = uiMessageDao.updateMessageStatus(messageId, messageStatus, userMessageLog.getDeleted(),
                    userMessageLog.getNextAttempt(), userMessageLog.getFailed(), new Date(jmsTimestamp));
            if (updateSuccess) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}Message with messageId=[{}] synced, status=[{}]",
                            MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                            messageStatus);
                }
                return;
            }

        }
        LOG.debug("messageStatusChange skipped for messageId=[{}]", messageId);
    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param notificationStatus
     * @param jmsTimestamp
     */
    @Override
    public void messageNotificationStatusChange(String messageId, NotificationStatus notificationStatus, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);

        if (entity == null) {
            LOG.warn(LOG_WARN_NO_RECORD_FOUND, messageId);
            return;
        }

        if (entity.getLastModified2() == null) {
            LOG.warn("LAST_MODIFIED2 is null for messageid=[{}]", messageId);
            return;
        }
        if (entity.getLastModified2().getTime() <= jmsTimestamp) {
            boolean updateSuccess = uiMessageDao.updateNotificationStatus(messageId, notificationStatus, new Date(jmsTimestamp));
            if (updateSuccess) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}Message with messageId=[{}] synced, notificationStatus=[{}]",
                            MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId,
                            notificationStatus);
                }
                return;
            }
        }
        LOG.debug("messageNotificationStatusChange skipped for messageId=[{}]", messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void messageChange(String messageId, long jmsTimestamp) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);
        final Date jmsTime = new Date(jmsTimestamp);

        if (entity == null) {
            LOG.warn(LOG_WARN_NO_RECORD_FOUND, messageId);
            return;
        }

        if (entity.getLastModified() == null) {
            LOG.warn("LAST_MODIFIED is null for messageid=[{}]", messageId);
            return;
        }

        if (entity.getLastModified().getTime() <= jmsTimestamp) {
            boolean updateSuccess = uiMessageDao.updateMessage(messageId, userMessageLog.getMessageStatus(),
                    userMessageLog.getDeleted(), userMessageLog.getFailed(), userMessageLog.getRestored(),
                    userMessageLog.getNextAttempt(), userMessageLog.getSendAttempts(), userMessageLog.getSendAttemptsMax(),
                    jmsTime);
            if (updateSuccess) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}Message with messageId=[{}] synced",
                            MessageType.USER_MESSAGE.equals(userMessageLog.getMessageType()) ? "User" : "Signal", messageId);
                }
                return;
            }
        }
        LOG.debug("messageChange skipped for messageId=[{}]", messageId);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void signalMessageSubmitted(String messageId, long jmsTimestamp) {
        LOG.debug("SignalMessage=[{}] submitted", messageId);
        saveUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void signalMessageReceived(String messageId, long jmsTimestamp) {
        LOG.debug("SignalMessage=[{}] received", messageId);
        saveUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
    }


    /**
     * Replicates {@link SignalMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void saveUIMessageFromSignalMessageLog(String messageId, final long jmsTimestamp) {
        final MessageLog signalMessageLog = signalMessageLogDao.findByMessageId(messageId);
        final SignalMessage signalMessage = messagingDao.findSignalMessageByMessageId(messageId);

        final Messaging messaging = messagingDao.findMessageByMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        final UserMessage userMessage = messaging.getUserMessage();

        UIMessageEntity entity = createUIMessageEntity(messageId, jmsTimestamp, signalMessageLog, userMessage);
        entity.setRefToMessageId(signalMessage.getMessageInfo().getRefToMessageId());
        entity.setConversationId(StringUtils.EMPTY);
        uiMessageDao.create(entity);

        LOG.debug("SignalMessage with messageId=[{}] inserted", messageId);
    }

    /**
     * Replicates {@link UserMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    protected void saveUIMessageFromUserMessageLog(String messageId, long jmsTimestamp) {
        final MessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        UIMessageEntity entity = createUIMessageEntity(messageId, jmsTimestamp, userMessageLog, userMessage);
        entity.setRefToMessageId(userMessage.getMessageInfo().getRefToMessageId());
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());

        uiMessageDao.create(entity);

        LOG.debug("UserMessage with messageId=[{}] inserted", messageId);
    }

    private UIMessageEntity createUIMessageEntity(String messageId, long jmsTimestamp, MessageLog messageLog, UserMessage userMessage) {
        //using Dozer
        UIMessageEntity entity = domainConverter.convert(messageLog, UIMessageEntity.class);

        entity.setEntityId(0); //dozer
        entity.setMessageId(messageId);

        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setLastModified(new Date(jmsTimestamp));
        entity.setLastModified2(new Date(jmsTimestamp));

        return entity;
    }

}
