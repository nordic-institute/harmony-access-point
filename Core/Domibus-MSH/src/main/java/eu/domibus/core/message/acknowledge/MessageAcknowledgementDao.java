package eu.domibus.core.message.acknowledge;

import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Repository
public class MessageAcknowledgementDao extends BasicDao<MessageAcknowledgementEntity> {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgementDao.class);

    public MessageAcknowledgementDao() {
        super(MessageAcknowledgementEntity.class);
    }

    public List<MessageAcknowledgementEntity> findByMessageId(String messageId) {
        try {
            final TypedQuery<MessageAcknowledgementEntity> query = em.createNamedQuery("MessageAcknowledgement.findMessageAcknowledgementByMessageId",
                    MessageAcknowledgementEntity.class);
            query.setParameter("MESSAGE_ID", messageId);
            return query.getResultList();
        } catch (NoResultException e) {
            LOG.debug("Could not find any message acknowledge for message id[" + messageId + "]");
            return null;
        }
    }

    @Timer(clazz = MessageAcknowledgementDao.class,value = "deleteMessages.deleteMessageAcknowledgementsByMessageIds")
    @Counter(clazz = MessageAcknowledgementDao.class,value = "deleteMessages.deleteMessageAcknowledgementsByMessageIds")
    public int deleteMessageAcknowledgementsByMessageIds(List<Long> messageIds) {
        final Query deleteQuery = em.createNamedQuery("MessageAcknowledgement.deleteMessageAcknowledgementsByMessageIds");
        deleteQuery.setParameter("IDS", messageIds);
        int result = deleteQuery.executeUpdate();
        LOG.trace("deleteMessageAcknowledgementsByMessageIds result [{}]", result);
        return result;
    }

}
