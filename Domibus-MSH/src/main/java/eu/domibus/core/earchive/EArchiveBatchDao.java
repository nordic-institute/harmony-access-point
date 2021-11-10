package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.earchive.EArchiveRequestType.CONTINUOUS;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class EArchiveBatchDao extends BasicDao<EArchiveBatchEntity> {

    public EArchiveBatchDao() {
        super(EArchiveBatchEntity.class);
    }

    public EArchiveBatchEntity findEArchiveBatchByBatchEntityId(long entityId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByEntityId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ENTITY_ID", entityId);
        return getFirstResult(query);
    }

    public EArchiveBatchEntity findEArchiveBatchByBatchId(String batchId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByBatchId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ID", batchId);
        return getFirstResult(query);
    }

    protected <T> T getFirstResult(TypedQuery<T> query) {
        List<T> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    public Long findLastEntityIdArchived() {
        TypedQuery<Long> query = this.em.createNamedQuery("EArchiveBatchEntity.findLastEntityIdArchived", Long.class);
        query.setParameter("REQUEST_TYPE", CONTINUOUS);
        List<Long> resultList = query.getResultList();
        if (isEmpty(resultList)) {
            return null;
        }
        return resultList.get(0);
    }

    @Transactional
    public EArchiveBatchEntity setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status, String error, String errorCode) {
        eArchiveBatchByBatchId.setEArchiveBatchStatus(status);
        eArchiveBatchByBatchId.setErrorMessage(error);
        eArchiveBatchByBatchId.setErrorCode(errorCode);
        return merge(eArchiveBatchByBatchId);
    }

    public <T extends EArchiveBatchBaseEntity> List<T> getBatchRequestList(EArchiveBatchFilter filter, Class<T> clazzProjection) {

        CriteriaQuery<T> batchCriteriaQuery = getEArchiveBatchCriteriaQuery(filter, false, clazzProjection);
        TypedQuery<T> batchQuery = em.createQuery(batchCriteriaQuery);
        // set pagination
        setPaginationParametersToQuery(batchQuery, filter.getPageStart(), filter.getPageSize());
        return batchQuery.getResultList();
    }

    public Long getBatchRequestListCount(EArchiveBatchFilter filter) {
        CriteriaQuery<Long> countQuery = getEArchiveBatchCriteriaQuery(filter, true, EArchiveBatchSummaryEntity.class);
        return em.createQuery(countQuery).getSingleResult();
    }

    public List<UserMessageDTO> getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        TypedQuery<UserMessageDTO> query = this.em.createNamedQuery("UserMessageLog.findMessagesForArchivingAsc", UserMessageDTO.class);
        query.setParameter("LAST_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("MAX_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("STATUSES", MessageStatus.getFinalStates());
        setPaginationParametersToQuery(query, pageStart, pageSize);
        return query.getResultList();
    }

    public Long getNotArchivedMessageCountForPeriod(Date messageStartDate, Date messageEndDate) {
        TypedQuery<Long> query = em.createNamedQuery("UserMessageLog.countMessagesForArchiving", Long.class);
        query.setParameter("LAST_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("MAX_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("STATUSES", MessageStatus.getFinalStates());
        return query.getSingleResult();
    }

    public List<UserMessageDTO> getBatchMessageList(String batchId, Integer pageStart, Integer pageSize) {
        TypedQuery<UserMessageDTO> query = em.createNamedQuery("EArchiveBatchRequest.getMessagesForBatchId", UserMessageDTO.class);
        query.setParameter("batchId", batchId);
        setPaginationParametersToQuery(query, pageStart, pageSize);
        return query.getResultList();
    }

    /**
     * Set pagination values to query
     *
     * @param query
     * @param pageStart
     * @param pageSize
     */
    public <T> void setPaginationParametersToQuery(TypedQuery<T> query, Integer pageStart, Integer pageSize) {

        // if page is not set start with the fist page
        int iMaxResults = pageSize == null || pageSize < 0 ? 0 : pageSize;
        int startingAt = (pageStart == null || pageStart < 0 ? 0 : pageStart) * iMaxResults;
        if (startingAt > 0) {
            query.setFirstResult(startingAt);
        }
        if (iMaxResults > 0) {
            query.setMaxResults(iMaxResults);
        }
    }

    /**
     * Returns criteria builder for filter.
     *
     * @param filter
     * @param queryProjectionForCount - if true then result is count of the results, else query returns list of entities
     * @return
     */
    protected <T extends EArchiveBatchBaseEntity> CriteriaQuery getEArchiveBatchCriteriaQuery(EArchiveBatchFilter filter, boolean queryProjectionForCount, Class<T> clazzProjection) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery criteria = queryProjectionForCount ? builder.createQuery(Long.class) : builder.createQuery(clazzProjection);

        final Root<T> eArchiveBatchRoot = criteria.from(clazzProjection);
        List<Predicate> predicates = new ArrayList<>();

        if (queryProjectionForCount) {
            criteria.select(builder.count(eArchiveBatchRoot));
        } else {
            // sort results by EArchiveBatchEntity DateRequested.
            criteria.orderBy(builder.desc(eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.dateRequested)));
        }

        // filter by batch request date
        if (filter.getStartDate() != null) {
            predicates.add(builder.greaterThanOrEqualTo(eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.dateRequested), filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(builder.lessThan(eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.dateRequested), filter.getEndDate()));
        }
        // the "batch MessageSId" is a range. Check if start and end message Id falls into the range
        if (filter.getMessageStarId() != null) {
            predicates.add(builder.greaterThan(eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.lastPkUserMessage), filter.getMessageStarId()));
        }
        if (filter.getMessageEndId() != null) {
            predicates.add(builder.lessThan(eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.firstPkUserMessage), filter.getMessageEndId()));
        }

        // filter by type
        if (filter.getRequestTypes() != null && !filter.getRequestTypes().isEmpty()) {
            Expression<EArchiveRequestType> statusExpression = eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.requestType);
            predicates.add(statusExpression.in(filter.getRequestTypes()));
        }

        // filter by batch status list.
        if (filter.getStatusList() != null && !filter.getStatusList().isEmpty()) {
            Expression<EArchiveBatchStatus> statusExpression = eArchiveBatchRoot.get(EArchiveBatchBaseEntity_.eArchiveBatchStatus);
            predicates.add(statusExpression.in(filter.getStatusList()));
        }

        // set all predicates to the select
        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        return criteria;
    }
}
