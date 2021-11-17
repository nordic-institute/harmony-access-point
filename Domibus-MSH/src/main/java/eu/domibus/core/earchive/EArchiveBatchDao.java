package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.core.util.QueryUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final QueryUtil queryUtil;

    public EArchiveBatchDao(QueryUtil queryUtil) {
        super(EArchiveBatchEntity.class);
        this.queryUtil = queryUtil;
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

    @Transactional
    public void expireBatches(final Date limitDate) {
        final Query query = em.createNamedQuery("EArchiveBatchEntity.updateStatusByDate");
        query.setParameter("LIMIT_DATE", limitDate);
        query.setParameter("STATUSES", Arrays.asList(
                EArchiveBatchStatus.EXPORTED));
        query.setParameter("NEW_STATUS", EArchiveBatchStatus.EXPIRED);
        query.executeUpdate();
    }

    public List<EArchiveBatchEntity> findBatchesByStatus(List<EArchiveBatchStatus> statuses, Integer pageSize) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByStatus", EArchiveBatchEntity.class);
        query.setParameter("STATUSES", statuses);
        queryUtil.setPaginationParametersToQuery(query, 0, pageSize);
        return query.getResultList();
    }

    public List<EArchiveBatchEntity> getBatchRequestList(EArchiveBatchFilter filter) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<EArchiveBatchEntity> criteria = builder.createQuery(EArchiveBatchEntity.class);

        final Root<EArchiveBatchEntity> eArchiveBatchRoot = criteria.from(EArchiveBatchEntity.class);

        criteria.orderBy(builder.desc(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested)));

        List<Predicate> predicates = getPredicates(filter, builder, eArchiveBatchRoot);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        TypedQuery<EArchiveBatchEntity> batchQuery = em.createQuery(criteria);

        queryUtil.setPaginationParametersToQuery(batchQuery, filter.getPageStart(), filter.getPageSize());

        return batchQuery.getResultList();
    }

    public Long getBatchRequestListCount(EArchiveBatchFilter filter) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        Root<EArchiveBatchEntity> eArchiveBatchRoot = criteria.from(EArchiveBatchEntity.class);

        criteria.select(builder.count(eArchiveBatchRoot));

        List<Predicate> predicates = getPredicates(filter, builder, eArchiveBatchRoot);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        return em.createQuery(criteria).getSingleResult();
    }

    public List<EArchiveBatchUserMessage> getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        TypedQuery<EArchiveBatchUserMessage> query = this.em.createNamedQuery("UserMessageLog.findMessagesForArchivingAsc", EArchiveBatchUserMessage.class);
        query.setParameter("LAST_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("MAX_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("STATUSES", MessageStatus.getFinalStates());
        queryUtil.setPaginationParametersToQuery(query, pageStart, pageSize);
        return query.getResultList();
    }

    public Long getNotArchivedMessageCountForPeriod(Date messageStartDate, Date messageEndDate) {
        TypedQuery<Long> query = em.createNamedQuery("UserMessageLog.countMessagesForArchiving", Long.class);
        query.setParameter("LAST_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageStartDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("MAX_ENTITY_ID", Long.parseLong(ZonedDateTime.ofInstant(messageEndDate.toInstant(), ZoneOffset.UTC).format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX));
        query.setParameter("STATUSES", MessageStatus.getFinalStates());
        return query.getSingleResult();
    }

    private List<Predicate> getPredicates(EArchiveBatchFilter filter, CriteriaBuilder builder, Root<EArchiveBatchEntity> eArchiveBatchRoot) {
        List<Predicate> predicates = new ArrayList<>();
        // filter by batch request date
        if (filter.getStartDate() != null) {
            predicates.add(builder.greaterThanOrEqualTo(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested), filter.getStartDate()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(builder.lessThan(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested), filter.getEndDate()));
        }
        // the "batch MessageSId" is a range. Check if start and end message Id falls into the range
        if (filter.getMessageStarId() != null) {
            predicates.add(builder.greaterThan(eArchiveBatchRoot.get(EArchiveBatchEntity_.lastPkUserMessage), filter.getMessageStarId()));
        }
        if (filter.getMessageEndId() != null) {
            predicates.add(builder.lessThan(eArchiveBatchRoot.get(EArchiveBatchEntity_.firstPkUserMessage), filter.getMessageEndId()));
        }

        // filter by type
        if (filter.getRequestTypes() != null && !filter.getRequestTypes().isEmpty()) {
            Expression<EArchiveRequestType> statusExpression = eArchiveBatchRoot.get(EArchiveBatchEntity_.requestType);
            predicates.add(statusExpression.in(filter.getRequestTypes()));
        }

        // filter by batch status list.
        if (filter.getStatusList() != null && !filter.getStatusList().isEmpty()) {
            Expression<EArchiveBatchStatus> statusExpression = eArchiveBatchRoot.get(EArchiveBatchEntity_.eArchiveBatchStatus);
            predicates.add(statusExpression.in(filter.getStatusList()));
        }
        return predicates;
    }
}
