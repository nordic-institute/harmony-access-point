package eu.domibus.api.multitenancy.lock;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.QueryTimeoutException;
import java.util.concurrent.Callable;

/**
 * Wrapper for the Runnable class to be executed. Attempts to lock via db record and in case it succeeds it runs the wrapped Runnable.
 * <p>
 * IMPORTANT: Only use with tasks that are short in duration since the locking requires an active database transaction
 * <p>
 * If using the SynchronizationServiceImpl as synchronizationService then the synchronization will be done using a pessimistic lock,
 * so a transaction has to be active to acquire the lock initially and has to end only after the runnable task has finished.
 * Instantiate using SynchronizedRunnableFactory, so it can be managed by Spring which needs to start the transaction required by the run method.
 * Note that Spring transactions are thread specific so any exceptions on this thread will not roll back the transaction of the parent thread.
 * See eu.domibus.core.SynchronizedRunnableIT for detailed behaviour in case of exceptions.
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public class DBClusterSynchronizedRunnable<T> implements Runnable, Callable<T> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DBClusterSynchronizedRunnable.class);

    private final DBSynchronizationHelper dbSynchronizationHelper;

    private final String lockKey;

    private Runnable runnable;

    private Callable<T> callable;

    private DBClusterSynchronizedRunnable(String lockKey, DBSynchronizationHelper dbSynchronizationHelper) {
        this.lockKey = lockKey;
        this.dbSynchronizationHelper = dbSynchronizationHelper;
    }

    /**
     * Instantiate using SynchronizedRunnableFactory, so it can be managed by Spring which needs to start the transaction required by the run method.
     *
     * @param runnable                the task to execute (short duration)
     * @param lockKey                 the key for which to lock execution
     * @param dbSynchronizationHelper service used to acquire the lock
     */
    protected DBClusterSynchronizedRunnable(Runnable runnable, String lockKey, DBSynchronizationHelper dbSynchronizationHelper) {
        this(lockKey, dbSynchronizationHelper);
        this.runnable = runnable;
    }

    protected DBClusterSynchronizedRunnable(Callable<T> callable, String lockKey, DBSynchronizationHelper dbSynchronizationHelper) {
        this(lockKey, dbSynchronizationHelper);
        this.callable = callable;
    }

    @Override
    @Transactional
    public void run() {
        try {
            executeTask(this::wrappedRunnable, false);
        } catch (Exception e) {
            throw new DomibusSynchronizationException(e);
        }
    }

    @Override
    @Transactional
    public T call() throws Exception {
        return executeTask(() -> callable.call(), false);
    }

    private <T> T executeTask(Callable<T> task, boolean swallowException) throws Exception {
        LOG.debug("Trying to lock [{}]", lockKey);

        String threadName = Thread.currentThread().getName();
        Thread.currentThread().setName(lockKey + "-" + System.nanoTime());

        T res = null;
        try {
            // if this blocks, it means that another process has a write lock on the db record
            dbSynchronizationHelper.acquireLock(lockKey);
            LOG.debug("Acquired lock on key [{}]", lockKey);

            LOG.debug("Start executing task with db lock");
            res = task.call();
            LOG.debug("Finished executing task with db lock");
        } catch (NoResultException nre) {
            throw new DomibusSynchronizationException(String.format("Lock key [%s] not found!", lockKey), nre);
        } catch (LockTimeoutException | QueryTimeoutException te) {
            LOG.warn("[{}] key lock could not be acquired. It is probably being used by another process.", lockKey);
            throw new DomibusSynchronizationException(lockKey + " key lock could not be acquired. It is probably being used by another process." , te);
        } catch (Exception ex) {
            if (!swallowException) {
                throw new DomibusSynchronizationException("Error executing a task with db lock:" + lockKey, ex);
            }
            LOG.error("Error while running synchronized task.", ex);
        }

        Thread.currentThread().setName(threadName);
        return res;
    }

    private Boolean wrappedRunnable() {
        runnable.run();
        return true;
    }
}
