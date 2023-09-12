package eu.domibus.api.multitenancy.lock;

import java.util.concurrent.Callable;

/**
 * @author Ion Perpegel
 * @since 5.2
 */
public interface SynchronizationService {

    /**
     * Returns a task that is a synchronized wrapper of the original task,meaning that this task will sync the execution of the task received as a parameter
     *
     * @param task the task to excute with lock
     * @param dbLockKey the lock key in the TB_LOCK table
     * @param javaLockKey java object instance to sync on
     * @param <T> the type of the returned value of the task itself
     * @return the synchronized wrapper of the original task
     */
    <T> Callable<T> getSynchronizedCallable(Callable<T> task, String dbLockKey, String javaLockKey);

    /**
     * Executes the given task synchronously, with one of the following locking mechanisms:
     * Java synchronized locking in case of standalone deployment
     * Database locking in case of a cluster deployment
     *
     * @param task the task to execute with lock
     * @param dbLockKey the lock key in the TB_LOCK table
     * @param javaLockKey java object instance to sync on
     * @param <T> the type of the returned value of the task itself
     * @return the returned value of the task
     */
    <T> T execute(final Callable<T> task, final String dbLockKey, final String javaLockKey);

    void execute(final Runnable task, final String dbLockKey, final String javaLockKey);

    void execute(final Runnable task, final String dbLockKey);
}
