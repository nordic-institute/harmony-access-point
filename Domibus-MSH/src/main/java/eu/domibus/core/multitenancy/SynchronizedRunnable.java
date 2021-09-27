package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.core.spring.Lock;
import eu.domibus.core.spring.LockDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;

/**
 * Wrapper for the Runnable class to be executed. Attempts to lock the file and in case it succeeds it runs the wrapped Runnable
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public class SynchronizedRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnable.class);

    protected LockDao lockDao;
    private String lockKey;
    private Runnable runnable;

    public SynchronizedRunnable(Runnable runnable, String lockKey, LockDao lockDao) {
        this.runnable = runnable;
        this.lockKey = lockKey;
        this.lockDao = lockDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run() {
        LOG.trace("Trying to lock [{}]", lockKey);

        try {
            // if this blocks, it means that another process has a write lock on the db record
            Lock lock = lockDao.acquireLock(lockKey);
            LOG.trace("Acquired lock on key [{}]", lockKey);

            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");
        } catch (NoResultException nre) {
            throw new DomainTaskException(String.format("Lock key [%s] not found!", lockKey), nre);
        } catch (LockTimeoutException lte) {
            LOG.warn("[{}] key lock could not be acquired. It is probably used by another process.", lockKey, lte);
        } catch (Exception ex) {
            LOG.error("Error while running synchronized task.", ex);
        }
    }
}
