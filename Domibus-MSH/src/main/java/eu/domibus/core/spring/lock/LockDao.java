package eu.domibus.core.spring.lock;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Repository
public class LockDao extends BasicDao<LockEntity> {

    public LockDao() {
        super(LockEntity.class);
    }

    public LockEntity findByLockKeyWithLock(String lockKey) {
        Query q = em.createNamedQuery("Lock.findByLockName", LockEntity.class);
        q.setParameter("LOCK_KEY", lockKey);
        return (LockEntity) q.getSingleResult();
    }

}
