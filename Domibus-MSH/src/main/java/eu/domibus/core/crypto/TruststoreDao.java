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

    public Truststore findByType(String type) {
        Query q = em.createNamedQuery("Truststore.findByType", Truststore.class);
        q.setParameter("TYPE", type);
        return (Truststore) q.getSingleResult();
    }

    public boolean existsWithType(String type) {
        Query q = em.createNamedQuery("Truststore.countByType", Long.class);
        q.setParameter("TYPE", type);
        return (Long) q.getSingleResult() > 0;
    }

}
