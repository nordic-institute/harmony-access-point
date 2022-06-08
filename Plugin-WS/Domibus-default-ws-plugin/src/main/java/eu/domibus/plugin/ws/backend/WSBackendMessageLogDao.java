package eu.domibus.plugin.ws.backend;

import eu.domibus.ext.services.DateExtService;
import eu.domibus.plugin.ws.util.WSBasicDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.plugin.ws.backend.WSBackendMessageStatus.SEND_FAILURE;

/**
 * @author idragusa
 * @since 4.2
 */
@Repository
public class WSBackendMessageLogDao extends WSBasicDao<WSBackendMessageLogEntity> {

    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CRIT_MESSAGE_ID = "messageId";
    private static final String CRIT_FINAL_RECIPIENT = "finalRecipient";
    private static final String CRIT_CREATION_TIME = "creationTime";
    private static final String CRIT_ORIGINAL_SENDER = "originalSender";
    public static final String CRIT_BACKEND_MESSAGE_STATUS = "backendMessageStatus";

    private final DateExtService dateExtService;

    public WSBackendMessageLogDao(DateExtService dateExtService) {
        super(WSBackendMessageLogEntity.class);
        this.dateExtService = dateExtService;
    }

    /**
     * Find the entry based on a given MessageId.
     *
     * @param messageId the id of the message.
     */
    public WSBackendMessageLogEntity findByMessageId(String messageId) {
        TypedQuery<WSBackendMessageLogEntity> query = em.createNamedQuery(
                "WSBackendMessageLogEntity.findByMessageId",
                WSBackendMessageLogEntity.class);
        query.setParameter(MESSAGE_ID, messageId);
        WSBackendMessageLogEntity wsBackendMessageLogEntity;
        try {
            wsBackendMessageLogEntity = query.getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
        return wsBackendMessageLogEntity;
    }

    /**
     * Find the backend messages with:
     * <p>
     * {@link WSBackendMessageLogEntity#getBackendMessageStatus()} ()} = {@code WSBackendMessageStatus.WAITING_FOR_RETRY}
     * {@link WSBackendMessageLogEntity#getNextAttempt()} < {@code CURRENT_TIMESTAMP}
     * 0 < {@link WSBackendMessageLogEntity#getSendAttempts()} < {@link WSBackendMessageLogEntity#getSendAttemptsMax()}
     * {@link WSBackendMessageLogEntity#getScheduled()} is {@code false} or {@code null}
     *
     * @return list of backend messages available for retry now
     */
    public List<WSBackendMessageLogEntity> findRetryMessages() {
        TypedQuery<WSBackendMessageLogEntity> query = em.createNamedQuery(
                "WSBackendMessageLogEntity.findRetryMessages",
                WSBackendMessageLogEntity.class);
        query.setParameter("CURRENT_TIMESTAMP", dateExtService.getUtcDate());
        query.setParameter("BACKEND_MESSAGE_STATUS", WSBackendMessageStatus.WAITING_FOR_RETRY);

        return query.getResultList();
    }

    public WSBackendMessageLogEntity getById(long backendMessageEntityId) {
        return em.find(typeOfT, backendMessageEntityId);
    }

    public List<WSBackendMessageLogEntity> findAllFailedWithFilter(
            String messageId,
            String originalSender,
            String finalRecipient,
            LocalDateTime receivedFrom,
            LocalDateTime receivedTo,
            int maxCount) {
        TypedQuery<WSBackendMessageLogEntity> query = em.createQuery(
                buildWSMessageLogListCriteria(messageId,
                        originalSender, finalRecipient, receivedFrom, receivedTo, SEND_FAILURE));

        if (maxCount > 0) {
            query.setMaxResults(maxCount);
        }
        return query.getResultList();
    }

    protected CriteriaQuery<WSBackendMessageLogEntity> buildWSMessageLogListCriteria(
            String messageId,
            String originalSender,
            String finalRecipient,
            LocalDateTime receivedFrom,
            LocalDateTime receivedTo,
            WSBackendMessageStatus backendMessageStatus) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<WSBackendMessageLogEntity> criteriaQuery = criteriaBuilder.createQuery(WSBackendMessageLogEntity.class);
        Root<WSBackendMessageLogEntity> root = criteriaQuery.from(WSBackendMessageLogEntity.class);
        criteriaQuery.select(root);
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(messageId)) {
            predicates.add(criteriaBuilder.equal(root.get(CRIT_MESSAGE_ID), messageId));
        }
        if (StringUtils.isNotBlank(finalRecipient)) {
            predicates.add(criteriaBuilder.equal(root.get(CRIT_FINAL_RECIPIENT), finalRecipient));
        }
        if (StringUtils.isNotBlank(originalSender)) {
            predicates.add(criteriaBuilder.equal(root.get(CRIT_ORIGINAL_SENDER), originalSender));
        }
        if (receivedFrom != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(CRIT_CREATION_TIME), asDate(receivedFrom)));
        }
        if (receivedTo != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(CRIT_CREATION_TIME), asDate(receivedTo)));
        }
        if (backendMessageStatus != null) {
            predicates.add(criteriaBuilder.equal(root.get(CRIT_BACKEND_MESSAGE_STATUS), backendMessageStatus));
        }
        if (CollectionUtils.isNotEmpty(predicates)) {
            criteriaQuery.where(predicates.toArray(new Predicate[]{}));
        }
        criteriaQuery.orderBy(criteriaBuilder.asc(root.get(CRIT_CREATION_TIME)));
        return criteriaQuery;
    }

    private Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
    }

}
