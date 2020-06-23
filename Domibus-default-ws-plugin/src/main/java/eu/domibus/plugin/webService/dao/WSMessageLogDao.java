package eu.domibus.plugin.webService.dao;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * @author idragusa
 * @since 4.2
 */
@Repository
public class WSMessageLogDao extends WSBasicDao<WSMessageLogEntity> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSMessageLogDao.class);

    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String FINAL_RECIPIENT= "FINAL_RECIPIENT";

    public WSMessageLogDao() {
        super(WSMessageLogEntity.class);
    }

    /**
     * Find the entry based on a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public WSMessageLogEntity findByMessageId(String messageId) {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("WSMessageLogEntity.findByMessageId", WSMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSMessageLogEntity wsMessageLogEntity;
        try {
            wsMessageLogEntity = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsMessageLogEntity;
    }

    /**
     * Find all entries in the plugin table limited to maxCount. When maxCount is 0, return all.
     */
    public List<WSMessageLogEntity> findAll(int maxCount) {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("WSMessageLogEntity.findAll", WSMessageLogEntity.class);
        if(maxCount > 0) {
            return query.setMaxResults(maxCount).getResultList();
        }
        return query.getResultList();
    }

    /**
     * Fins all entries in the plugin table, for finalRecipient, limited to maxCount. When maxCount is 0, return all.
     */
    public List<WSMessageLogEntity> findAllByFinalRecipient(int maxCount, String finalRecipient) {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("WSMessageLogEntity.findAllByFinalRecipient", WSMessageLogEntity.class);
        query.setParameter(FINAL_RECIPIENT, finalRecipient);
        if(maxCount > 0) {
            return query.setMaxResults(maxCount).getResultList();
        }
        return query.getResultList();
    }

    /**
     * Find all entries in the plugin table.
     */
    public List<WSMessageLogEntity> findAll() {
        TypedQuery<WSMessageLogEntity> query = em.createNamedQuery("WSMessageLogEntity.findAll", WSMessageLogEntity.class);
        return query.getResultList();
    }

    /**
     * Delete the entry related to a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public void deleteByMessageId(final String messageId) {
        Query query = em.createNamedQuery("WSMessageLogEntity.deleteByMessageId");
        query.setParameter(MESSAGE_ID, messageId);
        query.executeUpdate();
    }
}
