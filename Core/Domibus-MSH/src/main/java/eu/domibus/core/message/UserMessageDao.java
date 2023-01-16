package eu.domibus.core.message;

import eu.domibus.api.messaging.DuplicateMessageFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageProperty;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.dictionary.ActionDictionaryService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
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
    public static final String PARTY_ID = "PARTY_ID";
    public static final String SENDER_PARTY_ID = "SENDER_PARTY_ID";
    public static final String ACTION_ID = "ACTION_ID";

    @Autowired
    private ActionDictionaryService actionDictionaryService;

    public UserMessageDao() {
        super(UserMessage.class);
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
        query.setParameter("MSH_ROLE", mshRole);
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
    public List<String> findAllPartitionsOlderThan(String partitionName, String dbUser) {
        Query q = em.createNamedQuery("UserMessage.findPartitionsForUser_ORACLE");
        q.setParameter("TNAME", UserMessage.TB_USER_MESSAGE);
        q.setParameter("PNAME", partitionName);
        q.setParameter("DB_USER", dbUser.toUpperCase());
        final List<String> partitionNames = q.getResultList();
        LOG.debug("Partitions [{}]", partitionNames);
        return partitionNames;
    }


    @Timer(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    @Counter(clazz = UserMessageDao.class, value = "findPotentialExpiredPartitions")
    public List<String> findAllPartitionsOlderThan(String partitionName) {
        Query q = em.createNamedQuery("UserMessage.findPartitions_ORACLE");
        q.setParameter("TNAME", UserMessage.TB_USER_MESSAGE);
        q.setParameter("PNAME", partitionName);
        final List<String> partitionNames = q.getResultList();
        LOG.debug("Partitions [{}]", partitionNames);
        return partitionNames;
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

    public UserMessage findLastTestMessageFromPartyToParty(String senderPartyId, String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageFromPartyToPartyDesc", UserMessage.class);
        query.setParameter(PARTY_ID, partyId);
        query.setParameter(SENDER_PARTY_ID, senderPartyId);
        query.setParameter("MSH_ROLE", MSHRole.SENDING);
        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<UserMessage> findTestMessagesToParty(String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageToPartyDesc", UserMessage.class);
        query.setParameter(PARTY_ID, partyId);
        query.setParameter("MSH_ROLE", MSHRole.SENDING);
        return query.getResultList();
    }

    public UserMessage findLastTestMessageToPartyWithStatus(String partyId, MessageStatus messageStatus) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findSentTestMessageWithStatusDesc", UserMessage.class);
        query.setParameter(PARTY_ID, partyId);
        query.setParameter("MSH_ROLE", MSHRole.SENDING);
        query.setParameter("STATUS", messageStatus);
        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<UserMessage> findTestMessagesFromParty(String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageFromPartyDesc", UserMessage.class);
        query.setParameter(PARTY_ID, partyId);
        query.setParameter("MSH_ROLE", MSHRole.RECEIVING);
        return query.getResultList();
    }

    public UserMessage findLastTestMessageFromParty(String partyId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findTestMessageFromPartyDesc", UserMessage.class);
        query.setParameter(PARTY_ID, partyId);
        query.setParameter("MSH_ROLE", MSHRole.RECEIVING);
        query.setMaxResults(1);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Boolean checkPartitionExists(String partitionName) {
        Query q = em.createNamedQuery("UserMessage.verifyPartitionExistsByName");
        q.setParameter("PNAME", partitionName);
        LOG.debug("Find partition [{}]", partitionName);

        try {
            Integer result = ((BigDecimal) DataAccessUtils.singleResult(q.getResultList())).intValue();
            if (result > 0) {
                LOG.debug("Partition exists [{}]", partitionName);
                return true;
            }
        } catch (Exception exp) {
            LOG.warn("Could not verify partition exists [{}]", partitionName, exp);
        }

        return false;
    }

}
