package eu.domibus.core.crypto;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Repository
public class TruststoreDao extends BasicDao<Truststore> {

    public TruststoreDao() {
        super(Truststore.class);
    }

    public Truststore findByName(String name) {
        Query q = em.createNamedQuery("Truststore.findByName", Truststore.class);
        q.setParameter("NAME", name);
        return (Truststore) q.getSingleResult();
    }

    public boolean existsWithName(String name) {
        Query q = em.createNamedQuery("Truststore.countByName", Long.class);
        q.setParameter("NAME", name);
        return (Long) q.getSingleResult() > 0;
    }

}
