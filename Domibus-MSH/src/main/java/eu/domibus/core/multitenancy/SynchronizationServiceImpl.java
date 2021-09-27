package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.SynchronizationService;
import eu.domibus.core.spring.LockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class SynchronizationServiceImpl implements SynchronizationService {

    @Autowired
    protected LockDao lockDao;

    @Override
    public void acquireLock(String lockKey) {
        lockDao.findByLockKey(lockKey);
    }
}
