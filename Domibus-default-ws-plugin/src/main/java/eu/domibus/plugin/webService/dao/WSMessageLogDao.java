package eu.domibus.plugin.webService.dao;

import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author idragusa
 * @since 4.2
 */
@Repository
public class WSMessageLogDao extends WSBasicDao<WSMessageLogEntity> {

    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String MESSAGE_IDS = "MESSAGE_IDS";
    private static final String FINAL_RECIPIENT= "FINAL_RECIPIENT";
    private static final String RECEIVED= "RECEIVED";


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
     * find all entries in plugin table based on criteria
     *
     * @param messageId
     * @param fromPartyId
     * @param conversationId
     * @param referenceMessageId
     * @param originalSender
     * @param finalRecipient
     * @param sendFrom
     * @param receivedUpTo
     * @param maxPendingMessagesRetrieveCount
     * @return List<WSMessageLogEntity>
     */
    public List<WSMessageLogEntity> findAllWithFilter(String messageId, String fromPartyId, String conversationId, String referenceMessageId,
                                                      String originalSender, String finalRecipient, Date sendFrom, LocalDateTime receivedUpTo,
                                                      int maxPendingMessagesRetrieveCount) {
        TypedQuery<WSMessageLogEntity> query = em.createQuery(
                buildWSMessageLogListCriteria(messageId, fromPartyId,conversationId, referenceMessageId,
                        originalSender, finalRecipient, sendFrom, receivedUpTo));

        query.setMaxResults(maxPendingMessagesRetrieveCount);
        return query.getResultList();

    }


    protected CriteriaQuery<WSMessageLogEntity> buildWSMessageLogListCriteria(String messageId, String fromPartyId, String conversationId,
                                                                              String referenceMessageId, String originalSender, String finalRecipient,
                                                                              Date sendFrom, LocalDateTime receivedUpTo) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<WSMessageLogEntity> criteriaQuery = criteriaBuilder.createQuery(WSMessageLogEntity.class);
        Root<WSMessageLogEntity> root = criteriaQuery.from(WSMessageLogEntity.class);
        criteriaQuery.select(root);
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(messageId)) {
            predicates.add(criteriaBuilder.equal(root.get(MESSAGE_ID), messageId));
        }
        if (StringUtils.isNotBlank(finalRecipient)) {
            predicates.add(criteriaBuilder.equal(root.get(FINAL_RECIPIENT), messageId));
        }
        if (receivedUpTo != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<LocalDateTime>get(RECEIVED), receivedUpTo));
        }

        if (CollectionUtils.isNotEmpty(predicates)) {
            criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        }
        return criteriaQuery;
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

    /**
     * Delete the entries related to a given MessageIds.
     *
     * @param messageIds the ids of the messages that should be deleted.
     */
    public void deleteByMessageIds(final List<String> messageIds) {
        Query query = em.createNamedQuery("WSMessageLogEntity.deleteByMessageIds");
        query.setParameter(MESSAGE_IDS, messageIds);
        query.executeUpdate();
    }
}
