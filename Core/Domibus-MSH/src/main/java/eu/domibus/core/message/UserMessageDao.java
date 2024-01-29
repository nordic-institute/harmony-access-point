package eu.domibus.core.message;

import eu.domibus.api.messaging.DuplicateMessageFoundException;
import eu.domibus.api.model.*;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.PartyIdDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class UserMessageDao extends BasicDao<UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDao.class);

    private static final String GROUP_ID = "GROUP_ID";

    public static final String MSH_ROLE = "MSH_ROLE";
    public static final String PARTY = "PARTY";

    private final MessageStatusDao messageStatusDao;

    private final MshRoleDao mshRoleDao;

    private final PartyIdDao partyIdDao;

    public UserMessageDao(MessageStatusDao messageStatusDao, MshRoleDao mshRoleDao, PartyIdDao partyIdDao) {
        super(UserMessage.class);
        this.messageStatusDao = messageStatusDao;
        this.mshRoleDao = mshRoleDao;
        this.partyIdDao = partyIdDao;
    }

    @Transactional(readOnly = true)
    public UserMessage findByEntityId(Long entityId) {
        final UserMessage userMessage = super.read(entityId);

        if (userMessage != null) {
            initializeChildren(userMessage);
        }

        return userMessage;
    }

    private void initializeChildren(UserMessage userMessage) {
        //initialize values from the second level cache
        final Set<MessageProperty> messageProperties = userMessage.getMessageProperties();
        if (CollectionUtils.isNotEmpty(messageProperties)) {
            messageProperties.forEach(messageProperty -> messageProperty.getValue());
        }
        userMessage.getMpcValue();
        userMessage.getServiceValue();
        userMessage.getActionValue();
        userMessage.getAgreementRefValue();
        if (userMessage.getPartyInfo() != null) {
            userMessage.getPartyInfo().getFrom().getFromPartyId().getValue();
            userMessage.getPartyInfo().getFrom().getRoleValue();
            userMessage.getPartyInfo().getTo().getToPartyId().getValue();
            userMessage.getPartyInfo().getTo().getRoleValue();
        }
    }

    @Transactional
    public UserMessage findByMessageId(String messageId, MSHRole mshRole) {
        if (mshRole == null) {
            return findByMessageId(messageId);
        }

        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByMessageIdAndRole", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        query.setParameter(MSH_ROLE, mshRoleDao.findByValue(mshRole));
        final UserMessage userMessage = DataAccessUtils.singleResult(query.getResultList());
        if (userMessage != null) {
            initializeChildren(userMessage);
        }
        return userMessage;
    }

    // we keep this until deprecated ext methods are deleted
    @Transactional
    public UserMessage findByMessageId(String messageId) {
        UserMessage result;
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        try {
            result = DataAccessUtils.singleResult(query.getResultList());
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new DuplicateMessageFoundException(messageId, ex);
        }
        if (result == null) {
            LOG.info("Query UserMessage.findByMessageId did not find any result for message with id [{}]", messageId);
            return null;
        }

        initializeChildren(result);
        return result;
    }

    public UserMessage findByGroupEntityId(Long groupEntityId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByGroupEntityId", UserMessage.class);
        query.setParameter("ENTITY_ID", groupEntityId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<UserMessage> findUserMessageByGroupId(final String groupId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findUserMessageByGroupId", UserMessage.class);
        query.setParameter(GROUP_ID, groupId);
        return query.getResultList();
    }

    @Timer(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    @Counter(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    public List<DatabasePartition> findAllPartitions(String dbUser) {
        Query q = em.createNamedQuery("UserMessage.findPartitionsForUser_ORACLE");
        q.setParameter("TNAME", UserMessage.TB_USER_MESSAGE);
        q.setParameter("DB_USER", dbUser.toUpperCase());
        final List<DatabasePartition> partitions = q.getResultList();
        LOG.debug("Partitions [{}]", partitions);
        return partitions;
    }


    @Timer(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    @Counter(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    public List<DatabasePartition> findAllPartitions() {
        Query q = em.createNamedQuery("UserMessage.findPartitions_ORACLE");
        q.setParameter("TNAME", UserMessage.TB_USER_MESSAGE);
        final List<DatabasePartition> partitions = q.getResultList();
        LOG.debug("Partitions [{}]", partitions);
        return partitions;
    }

    @Timer(clazz = UserMessageDao.class, value = "dropPartition")
    @Counter(clazz = UserMessageDao.class, value = "dropPartition")
    @Transactional
    public void dropPartition(String partitionName) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("DROP_PARTITION")
                .registerStoredProcedureParameter(
                        "partition_name",
                        String.class,
                        ParameterMode.IN
                )
                .setParameter("partition_name", partitionName);
        try {
            query.execute();
        } finally {
            try {
                query.unwrap(ProcedureOutputs.class).release();
                LOG.debug("Finished releasing drop partition procedure");
            } catch (Exception ex) {
                LOG.error("Finally exception when using the procedure to drop partitions", ex);
            }
        }
    }

    @Timer(clazz = UserMessageDao.class, value = "deleteMessages")
    @Counter(clazz = UserMessageDao.class, value = "deleteMessages")
    public int deleteMessages(List<Long> ids) {
        LOG.debug("deleteMessages [{}]", ids.size());
        final Query deleteQuery = em.createNamedQuery("UserMessage.deleteMessages");
        deleteQuery.setParameter("IDS", ids);
        int result = deleteQuery.executeUpdate();
        LOG.debug("deleteMessages result [{}]", result);
        return result;
    }

    public UserMessage findLastTestMessageFromPartyToParty(PartyId fromParty, PartyId toParty) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageFromPartyToPartyDesc", UserMessage.class);

        query.setParameter("SENDER_PARTY", fromParty);
        query.setParameter(PARTY, toParty);
        query.setParameter(MSH_ROLE, mshRoleDao.findByValue(MSHRole.SENDING));

        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<UserMessage> findTestMessagesToParty(String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageToPartyDesc", UserMessage.class);

        List<PartyId> partyEntities = partyIdDao.searchByValue(partyId);
        query.setParameter(PARTY, partyEntities);

        MSHRoleEntity roleEntity = mshRoleDao.findByValue(MSHRole.SENDING);
        query.setParameter(MSH_ROLE, roleEntity);

        return query.getResultList();
    }

    public UserMessage findLastTestMessageToPartyWithStatus(String partyId, MessageStatus messageStatus) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findSentTestMessageWithStatusDesc", UserMessage.class);

        List<PartyId> partyEntities = partyIdDao.searchByValue(partyId);
        query.setParameter(PARTY, partyEntities);

        MSHRoleEntity roleEntity = mshRoleDao.findByValue(MSHRole.SENDING);
        query.setParameter(MSH_ROLE, roleEntity);

        MessageStatusEntity statusEntity = messageStatusDao.findByValue(messageStatus);
        query.setParameter("STATUS", statusEntity);

        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<UserMessage> findTestMessagesFromParty(String partyId) {
        final TypedQuery<UserMessage> query = createTestMessagesFromPartyQuery(partyId);

        return query.getResultList();
    }

    public UserMessage findLastTestMessageFromParty(String partyId) {
        final TypedQuery<UserMessage> query = createTestMessagesFromPartyQuery(partyId);

        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    private TypedQuery<UserMessage> createTestMessagesFromPartyQuery(String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageFromPartyDesc", UserMessage.class);

        List<PartyId> partyEntities = partyIdDao.searchByValue(partyId);
        query.setParameter(PARTY, partyEntities);

        MSHRoleEntity roleEntity = mshRoleDao.findByValue(MSHRole.RECEIVING);
        query.setParameter(MSH_ROLE, roleEntity);

        return query;
    }

}
