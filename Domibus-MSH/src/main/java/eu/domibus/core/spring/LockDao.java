package eu.domibus.core.spring;

import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Repository
public class LockDao extends BasicDao<Lock> {

    public LockDao() {
        super(Lock.class);
    }

    public Lock acquireLock(String lockKey) {
        Query q = em.createNamedQuery("Lock.findByLockName", Lock.class);
        q.setParameter("LOCK_KEY", lockKey);
//        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return (Lock) q.getSingleResult();
    }

//    public void release(String lockKey) {
//        Lock lock = new Lock();
//        lock.setLockKey(lockKey);
//        delete(lock);
//    }

}
