package eu.domibus.core.message.splitandjoin;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Repository
public class MessageGroupDao extends BasicDao<MessageGroupEntity> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageGroupDao.class);

    public MessageGroupDao() {
        super(MessageGroupEntity.class);
    }

    public MessageGroupEntity findByGroupId(String groupId) {
        final TypedQuery<MessageGroupEntity> namedQuery = em.createNamedQuery("MessageGroupEntity.findByGroupId", MessageGroupEntity.class);
        namedQuery.setParameter("GROUP_ID", groupId);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException ex) {
            LOG.trace("Could not found MessageGroupEntity for group [{}]", groupId);
            return null;
        }
    }

    public List<MessageGroupEntity> findOngoingReceivedNonExpiredOrRejected() {
        TypedQuery<MessageGroupEntity> query = this.em.createNamedQuery("MessageGroupEntity.findReceivedNonExpiredOrRejected", MessageGroupEntity.class);
        query.setParameter("MSH_ROLE", MSHRole.RECEIVING);
        return query.getResultList();
    }

    public List<MessageGroupEntity> findOngoingSendNonExpiredOrRejected() {
        TypedQuery<MessageGroupEntity> query = this.em.createNamedQuery("MessageGroupEntity.findSendNonExpiredOrRejected", MessageGroupEntity.class);
        query.setParameter("MSH_ROLE", MSHRole.SENDING);
        query.setParameter("SOURCE_MSG_STATUS", MessageStatus.SEND_ENQUEUED);
        return query.getResultList();
    }
}
