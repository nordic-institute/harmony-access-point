package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * Task executor used to schedule tasks against a specific domain.
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface DomainTaskExtExecutor {


    /**
     * Submits a long running task to be executed for a specific domain
     *
     * @param task         The task to be executed
     * @param errorHandler The error handler to be executed in case errors are thrown while running the task
     * @param domain       The domain for which the task is executed
     */
    void submitLongRunningTask(Runnable task, Runnable errorHandler, DomainDTO domain);

    /**
     * Submits a long running task to be executed for a specific domain
     *
     * @param task   The task to be executed
     * @param domain The domain for which the task is executed
     */
    void submitLongRunningTask(Runnable task, DomainDTO domain);
}
