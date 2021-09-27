package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.core.spring.Lock;
import eu.domibus.core.spring.LockDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SynchronizedRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnable.class);

    @Autowired
    protected LockDao lockDao;

    private String lockKey;
    private Runnable runnable;

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

//    public SynchronizedRunnable(Runnable runnable, String lockKey, LockDao lockDao) {
//        this.runnable = runnable;
//        this.lockKey = lockKey;
//        this.lockDao = lockDao;
//    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void run() {
        LOG.trace("Trying to lock [{}]", lockKey);

        // if this fails, it means that another process has an explicit lock on the file
        try {
            Lock lock = lockDao.acquireLock(lockKey);
            LOG.trace("Acquired lock on key [{}]", lockKey);

            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");
        } catch (NoResultException nre) {
            throw new DomainTaskException(String.format("Lock key [%s] not found!", lockKey), nre);
        } catch (LockTimeoutException lte) {
            LOG.warn("[{}] lock could not be acquired. It is probably handled by another process.", lockKey, lte);
        } catch (Exception ex) {
            LOG.error("Error while running synchronized task", ex);
        }
    }
}
