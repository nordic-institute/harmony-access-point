package eu.domibus.core.crypto;

import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Repository
public class TruststoreDao extends BasicDao<TruststoreEntity> {

    public TruststoreDao() {
        super(TruststoreEntity.class);
    }

    public TruststoreEntity findByName(String name) {
        TypedQuery<TruststoreEntity> q = em.createNamedQuery("Truststore.findByName", TruststoreEntity.class);
        q.setParameter("NAME", name);
        return DataAccessUtils.singleResult(q.getResultList());
    }

    public TruststoreEntity findByNameSafely(String name) {
        try {
            return findByName(name);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean existsWithName(String name) {
        Query q = em.createNamedQuery("Truststore.countByName", Long.class);
        q.setParameter("NAME", name);
        return (Long) q.getSingleResult() > 0;
    }

}
