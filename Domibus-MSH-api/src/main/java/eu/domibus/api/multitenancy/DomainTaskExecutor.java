package eu.domibus.api.multitenancy;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Task executor used to schedule tasks that are issuing queries against the general schema from an already started transaction.
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainTaskExecutor {

    <T extends Object> T submit(Callable<T> task);

    void submit(Runnable task);

    Future<?> submit(Runnable task, boolean waitForTask);

    /**
     * Attempts to lock the file and if it succeeds submits a Runnable task for execution
     *
     * @param task The Runnable task to execute
     * @param errorHandler The Runnable task that will be executed in case an error occurs while running the main task
     * @param lockFile The file that will be locked before running the task
     */
    void submit(Runnable task, Runnable errorHandler, File lockFile);

    void submit(Runnable task, Domain domain);

    void submit(Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit);

    /**
     * Submits a long running task to be executed for a specific domain
     *
     * @param task         The task to be executed
     * @param errorHandler The error handler to be executed in case errors are thrown while running the task
     * @param domain       The domain for which the task is executed
     */
    void submitLongRunningTask(Runnable task, Runnable errorHandler, Domain domain);

    /**
     * Submits a long running task to be executed for a specific domain
     *
     * @param task   The task to be executed
     * @param domain The domain for which the task is executed
     */
    void submitLongRunningTask(Runnable task, Domain domain);
}
