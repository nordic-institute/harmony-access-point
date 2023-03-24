package eu.domibus.core.multitenancy.lock;

import eu.domibus.api.multitenancy.lock.SynchronizationService;
import eu.domibus.core.spring.lock.LockDao;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class SynchronizationServiceImpl implements SynchronizationService {

    final protected LockDao lockDao;

    public SynchronizationServiceImpl(LockDao lockDao) {
        this.lockDao = lockDao;
    }

    @Override
    public void acquireLock(String lockKey) {
        lockDao.findByLockKeyWithLock(lockKey);
    }
}
