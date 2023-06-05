package eu.domibus.api.multitenancy.lock;

import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;

/**
 * Wrapper for the Runnable class to be executed. Attempts to lock via db record and in case it succeeds it runs the wrapped Runnable.
 *
 * IMPORTANT: Only use with tasks that are short in duration since the locking requires an active database transaction
 *
 * If using the SynchronizationServiceImpl as synchronizationService then the synchronization will be done using a pessimistic lock,
 * so a transaction has to be active to acquire the lock initially and has to end only after the runnable task has finished.
 * Instantiate using SynchronizedRunnableFactory, so it can be managed by Spring which needs to start the transaction required by the run method.
 * Note that Spring transactions are thread specific so any exceptions on this thread will not roll back the transaction of the parent thread.
 * See eu.domibus.core.SynchronizedRunnableIT for detailed behaviour in case of exceptions.
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public class SynchronizedRunnable implements Runnable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnable.class);

    private SynchronizationService synchronizationService;
    private String lockKey;
    private Runnable runnable;

    /**
     * Instantiate using SynchronizedRunnableFactory, so it can be managed by Spring which needs to start the transaction required by the run method.
     * @param runnable the task to execute (short duration)
     * @param lockKey the key for which to lock execution
     * @param synchronizationService service used to acquire the lock
     */
    protected SynchronizedRunnable(Runnable runnable, String lockKey, SynchronizationService synchronizationService) {
        this.runnable = runnable;
        this.lockKey = lockKey;
        this.synchronizationService = synchronizationService;
    }

    @Override
    @Transactional
    public void run() {
        LOG.trace("Trying to lock [{}]", lockKey);

        String threadName = Thread.currentThread().getName();
        Thread.currentThread().setName(lockKey + "-" + System.nanoTime());

        try {
            // if this blocks, it means that another process has a write lock on the db record
            synchronizationService.acquireLock(lockKey);
            LOG.trace("Acquired lock on key [{}]", lockKey);

            LOG.trace("Start executing task");
            runnable.run();
            LOG.trace("Finished executing task");
        } catch (NoResultException nre) {
            throw new DomainTaskException(String.format("Lock key [%s] not found!", lockKey), nre);
        } catch (LockTimeoutException lte) {
            LOG.info("[{}] key lock could not be acquired. It is probably being used by another process.", lockKey);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[{}] key lock could not be acquired.", lockKey, lte);
            }
        } catch (Exception ex) {
            LOG.error("Error while running synchronized task.", ex);
        }

        Thread.currentThread().setName(threadName);
    }
}
