package eu.domibus.core.crypto;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

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
        Query q = em.createNamedQuery("Truststore.findByName", TruststoreEntity.class);
        q.setParameter("NAME", name);
        return (TruststoreEntity) q.getSingleResult();
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
