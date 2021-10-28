package eu.domibus.core.message;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DataBaseEngine;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class UserMessageDao extends BasicDao<UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDao.class);

    private static final String GROUP_ID = "GROUP_ID";

    public UserMessageDao() {
        super(UserMessage.class);
    }


    @Transactional(readOnly = true)
    public UserMessage findByEntityId(Long entityId) {
        final UserMessage userMessage = super.read(entityId);

        initializeChildren(userMessage);

        return userMessage;
    }

    private void initializeChildren(UserMessage userMessage) {
        //initialize values from the second level cache
        userMessage.getMessageProperties().forEach(messageProperty -> messageProperty.getValue());
        userMessage.getMpcValue();
        userMessage.getServiceValue();
        userMessage.getActionValue();
        userMessage.getAgreementRefValue();
        if(userMessage.getPartyInfo() != null) {
            userMessage.getPartyInfo().getFrom().getFromPartyId().getValue();
            userMessage.getPartyInfo().getFrom().getRoleValue();
            userMessage.getPartyInfo().getTo().getToPartyId().getValue();
            userMessage.getPartyInfo().getTo().getRoleValue();
        }
    }

    @Transactional
    public UserMessage findByMessageId(String messageId) {
        final TypedQuery<UserMessage> query = this.em.createNamedQuery("UserMessage.findByMessageId", UserMessage.class);
        query.setParameter("MESSAGE_ID", messageId);
        final UserMessage userMessage = DataAccessUtils.singleResult(query.getResultList());
        if(userMessage != null) {
            initializeChildren(userMessage);
        }
        return userMessage;
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
}
