package eu.domibus.core.message.attempt;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Repository
public class MessageAttemptDao extends BasicDao<MessageAttemptEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAttemptDao.class);

    public MessageAttemptDao() {
        super(MessageAttemptEntity.class);
    }

    public List<MessageAttemptEntity> findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAttemptEntity> query = em.createNamedQuery("MessageAttemptEntity.findAttemptsByMessageId", MessageAttemptEntity.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message attempts for message id[" + messageId + "]");
            return null;
        }
    }

    @Override
    public void create(MessageAttemptEntity entity) {
        entity.setError(StringUtils.abbreviate(entity.getError(), 255));
        super.create(entity);
    }

    public int deleteAttemptsByMessageIds(List<String> messageIds) {
        final Query deleteQuery = em.createNamedQuery("MessageAttemptEntity.deleteAttemptsByMessageIds");
        deleteQuery.setParameter("MESSAGEIDS", messageIds);
        int result  = deleteQuery.executeUpdate();
        LOG.info("deleteAttemptsByMessageIds result [{}]", result);
        return result;
    }

}
