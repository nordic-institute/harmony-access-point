package eu.domibus.core.replication;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.api.model.MessageLog;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.message.UserMessageDefaultServiceHelper;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
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

    static final String DOMIBUS_UI_REPLICATION_WAIT_BEFORE_UPDATE = "domibus.ui.replication.wait.before.update";

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

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;


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

        int timeToWait = getWaitTimeBeforePerformingUpdate();
        LOG.debug("wait [{}] ms and then start the update", timeToWait);
        try {
            //TODO EDELIVERY-5517
            //ugly stuff till we send messages with delay or implement another mechanism
            //updates are done in parallel and we need a delay as data may not be yet committed /visible
            Thread.sleep(timeToWait);
        } catch (InterruptedException e) {
            LOG.warn("exception while sleeping ", e);
            Thread.currentThread().interrupt();
        }

        //search for an existing record first
        final UIMessageEntity entity = uiMessageDao.findUIMessageByMessageId(messageId);
        if (entity == null) {
            LOG.warn(LOG_WARN_NO_RECORD_FOUND, messageId);
            return;
        }

        //run the update only if necessary
        if (entity.getLastModified() != null && entity.getLastModified().getTime() <= jmsTimestamp) {
            final UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
            boolean updateSuccess = uiMessageDao.updateMessage(userMessageLog,
                    jmsTimestamp);
            if (updateSuccess) {
                LOG.debug("{} updated", userMessageLog.getMessageType());
                return;
            }
        }
        LOG.debug("messageChange skipped for messageId=[{}]", messageId);
    }

    private int getWaitTimeBeforePerformingUpdate() {
        return domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_REPLICATION_WAIT_BEFORE_UPDATE);
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
