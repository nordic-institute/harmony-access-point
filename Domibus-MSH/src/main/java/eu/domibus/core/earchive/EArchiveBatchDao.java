package eu.domibus.core.earchive;

import eu.domibus.api.earchive.EArchiveBatchDTO;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.core.earchive.RequestType.CONTINUOUS;
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

    public EArchiveBatchEntity findEArchiveBatchByBatchId(long entityId) {
        TypedQuery<EArchiveBatchEntity> query = this.em.createNamedQuery("EArchiveBatchEntity.findByBatchId", EArchiveBatchEntity.class);
        query.setParameter("BATCH_ENTITY_ID", entityId);

        List<EArchiveBatchEntity> resultList = query.getResultList();
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
    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchByBatchId.seteArchiveBatchStatus(status);
        em.merge(eArchiveBatchByBatchId);
    }


    /** Returns criteria builder for filter.
     * @param filter
     * @param forCount - if true then result is count, else query returns list of entities
     * @return
     */
    protected CriteriaQuery getEArchiveBatchCriteriaQuery(EArchiveBatchFilter filter, boolean forCount) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery criteria = forCount ? builder.createQuery(Long.class) : builder.createQuery(EArchiveBatchEntity.class);

        final Root<EArchiveBatchEntity> eArchiveBatchRoot = criteria.from(EArchiveBatchEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        if (forCount) {
            criteria.select(builder.count(eArchiveBatchRoot));
        } else {
            // always sort by DateRequestedEArchiveBatchEntity

            criteria.orderBy(builder.desc(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested)));
        }

        if (filter.getEndDate() != null) {
            predicates.add(builder.lessThan(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested), filter.getEndDate()));
        }

        if (filter.getStartDate() != null) {
            predicates.add(builder.greaterThanOrEqualTo(eArchiveBatchRoot.get(EArchiveBatchEntity_.dateRequested), filter.getStartDate()));
        }

        if (StringUtils.isNotEmpty(filter.getRequestType())) {
            RequestType requestType = RequestType.valueOf(filter.getRequestType());
            predicates.add(builder.equal(eArchiveBatchRoot.get(EArchiveBatchEntity_.requestType), requestType));
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        return criteria;
    }

    public Long getQueuedBatchRequestsCount(EArchiveBatchFilter filter) {
        CriteriaQuery<Long> countQuery = getEArchiveBatchCriteriaQuery(filter, true);
        return em.createQuery(countQuery).getSingleResult();
    }

    public List<EArchiveBatchEntity> getQueuedBatchRequests(EArchiveBatchFilter filter) {
        CriteriaQuery<EArchiveBatchEntity>  batchCriteriaQuery = getEArchiveBatchCriteriaQuery(filter, false);
        TypedQuery<EArchiveBatchEntity> batchQuery = em.createQuery(batchCriteriaQuery);
        // set pagination
        int pageSize = filter.getPageSize()==null?0:filter.getPageSize();
        int startingAt = (filter.getPageStart()==null?0:filter.getPageStart()) * pageSize;
        if (filter.getPageSize() > 0) {
            batchQuery.setMaxResults(filter.getPageSize());
        }
        if (startingAt > 0) {
            batchQuery.setFirstResult(startingAt);
        }

        return batchQuery.getResultList();
    }

}
