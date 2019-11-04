package eu.domibus.core.replication;

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
 * Service dedicated to replicate
 * data in <code>TB_MESSAGE_UI</> table
 * It first reads existing data and then insert it
 *
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0
 */
@Service
public class UIReplicationDataServiceImpl implements UIReplicationDataService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationDataServiceImpl.class);

    static final String LOG_WARN_NO_RECORD_FOUND = "no record found in TB_MESSAGE_UI for messageId=[{}]";

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
    public void userMessageReceived(String messageId, long jmsTimestamp) {
        createUIMessageFromUserMessageLog(messageId, jmsTimestamp);
    }


    /**
     * {@inheritDoc}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    @Override
    public void userMessageSubmitted(String messageId, long jmsTimestamp) {
        LOG.debug("UserMessage=[{}] submitted", messageId);
        createUIMessageFromUserMessageLog(messageId, jmsTimestamp);
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
            boolean updateSuccess = uiMessageDao.updateMessage(userMessageLog,
                    jmsTime);
            if (updateSuccess) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}Message with messageId=[{}] updated",
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
        createUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
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
        createUIMessageFromSignalMessageLog(messageId, jmsTimestamp);
    }

    /**
     * Replicates {@link UserMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void createUIMessageFromUserMessageLog(String messageId, long jmsTimestamp) {
        final MessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);

        UIMessageEntity entity = createUIMessageEntity(messageId, jmsTimestamp, userMessageLog, userMessage);
        entity.setRefToMessageId(userMessage.getMessageInfo().getRefToMessageId());
        entity.setConversationId(userMessage.getCollaborationInfo().getConversationId());

        uiMessageDao.create(entity);

        LOG.debug("UserMessage with messageId=[{}] inserted", messageId);
    }

    /**
     * Replicates {@link SignalMessage} into {@code TB_MESSAGE_UI} table as {@link UIMessageEntity}
     *
     * @param messageId
     * @param jmsTimestamp
     */
    void createUIMessageFromSignalMessageLog(String messageId, final long jmsTimestamp) {
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

    private UIMessageEntity createUIMessageEntity(String messageId, long jmsTimestamp, MessageLog messageLog, UserMessage userMessage) {
        //domain converter
        UIMessageEntity entity = domainConverter.convert(messageLog, UIMessageEntity.class);

        entity.setEntityId(0); //be sure isn't set to null
        entity.setMessageId(messageId);

        entity.setFromId(userMessage.getPartyInfo().getFrom().getPartyId().iterator().next().getValue());
        entity.setToId(userMessage.getPartyInfo().getTo().getPartyId().iterator().next().getValue());
        entity.setFromScheme(userMessageDefaultServiceHelper.getOriginalSender(userMessage));
        entity.setToScheme(userMessageDefaultServiceHelper.getFinalRecipient(userMessage));
        entity.setLastModified(new Date(jmsTimestamp));

        return entity;
    }

}
