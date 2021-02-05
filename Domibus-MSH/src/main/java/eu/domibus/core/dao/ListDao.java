package eu.domibus.core.dao;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

/**
 * A basic DAO implementation providing standard list operations (filter, page, sort, count)
 *
 * @author Ion Perpegel
 * @since 4.1
 */
public abstract class ListDao<T extends AbstractBaseEntity> extends BasicDao<T> {

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public ListDao(final Class<T> typeOfT) {
        super(typeOfT);
    }

    protected abstract List<Predicate> getPredicates(Map<String, Object> filters, CriteriaBuilder cb, Root<T> ele);

    /**
     * Overridden solely in UIMessageDaoImpl, otherwise returns the parameter
     *
     * @param sortColumn
     * @return
     */
    protected String getSortColumn(String sortColumn) {
        return sortColumn;
    }

    public long countEntries(Map<String, Object> filters) {
        return countEntries(filters, 0);
    }

    public long countEntries(Map<String, Object> filters, int limit) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> mle = cq.from(typeOfT);
        cq.select(cb.count(mle));
        List<Predicate> predicates = getPredicates(filters, cb, mle);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Long> query = em.createQuery(cq);
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return query.getSingleResult();
    }

    public List<T> findPaged(final int from, final int max, final String sortColumn, final boolean asc, final Map<String, Object> filters) {
        final CriteriaBuilder cb = this.em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(typeOfT);
        final Root<T> ele = cq.from(typeOfT);
        cq.select(ele);
        List<Predicate> predicates = getPredicates(filters, cb, ele);
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        if (sortColumn != null) {
            String newSortColumn = getSortColumn(sortColumn);
            if (asc) {
                cq.orderBy(cb.asc(ele.get(newSortColumn)));
            } else {
                cq.orderBy(cb.desc(ele.get(newSortColumn)));
            }
        }
        final TypedQuery<T> query = this.em.createQuery(cq);
        query.setFirstResult(from);
        query.setMaxResults(max);
        return query.getResultList();
    }


}
