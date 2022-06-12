package eu.domibus.core.dao;

import eu.domibus.api.model.DomibusBaseEntity;
import eu.domibus.common.JPAConstants;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

/**
 * A basic DAO implementation providing the standard CRUD operations,
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini
 * @since 3.0
 */
public abstract class BasicDao<T extends DomibusBaseEntity> {

    protected final Class<T> typeOfT;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager em;

    /**
     * @param typeOfT The entity class this DAO provides access to
     */
    public BasicDao(final Class<T> typeOfT) {
        this.typeOfT = typeOfT;
    }

    public <T> T findById(Class<T> typeOfT, String id) {
        return em.find(typeOfT, id);
    }

    @Transactional
    public void create(final T entity) {
        em.persist(entity);
    }

    @Transactional
    public void delete(final T entity) {
        em.remove(em.merge(entity));
    }

    public T read(final long id) {
        return em.find(this.typeOfT, id);
    }

    public T findByReference(final long id) {
        return em.getReference(this.typeOfT, id);
    }

    @Transactional
    public void updateAll(final Collection<T> update) {
        for (final T t : update) {
            this.update(t);
        }
    }

    @Transactional
    public void deleteAll(final Collection<T> delete) {
        for (final T t : delete) {
            this.delete(t);
        }
    }

    @Transactional
    public void update(final T entity) {
        em.merge(entity);
    }

    public void flush() {
        em.flush();
    }

    public T merge(final T entity) {
        return em.merge(entity);
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(typeOfT);
        Root<T> rootEntry = cq.from(typeOfT);
        CriteriaQuery<T> all = cq.select(rootEntry);

        TypedQuery<T> allQuery = em.createQuery(all);
        return allQuery.getResultList();
    }

}
