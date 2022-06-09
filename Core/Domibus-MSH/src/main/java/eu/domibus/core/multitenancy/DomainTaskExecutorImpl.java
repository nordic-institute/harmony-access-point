package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.*;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnable;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnableFactory;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainTaskExecutorImpl implements DomainTaskExecutor {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainTaskExecutorImpl.class);
    public static final long DEFAULT_WAIT_TIMEOUT_IN_SECONDS = 60L;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Qualifier("taskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingTaskExecutor;

    @Qualifier("quartzTaskExecutor")
    @Autowired
    protected SchedulingTaskExecutor schedulingLongTaskExecutor;

    @Autowired
    SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Override
    public <T extends Object> T submit(Callable<T> task) {
        DomainCallable domainCallable = new DomainCallable(domainContextProvider, task);
        final Future<T> utrFuture = schedulingTaskExecutor.submit(domainCallable);
        try {
            return utrFuture.get(DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new DomainTaskException("Could not execute task", e);
        }
    }

    @Override
    public void submit(Runnable task) {
        LOG.trace("Submitting task");
        final ClearDomainRunnable clearDomainRunnable = new ClearDomainRunnable(domainContextProvider, task);
        submitRunnable(schedulingTaskExecutor, clearDomainRunnable, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public Future<?> submit(Runnable task, boolean waitForTask) {
        LOG.trace("Submitting task, waitForTask [{}]", waitForTask);
        final ClearDomainRunnable clearDomainRunnable = new ClearDomainRunnable(domainContextProvider, task);
        return submitRunnable(schedulingTaskExecutor, clearDomainRunnable, waitForTask, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Runnable errorHandler, String lockKey) {
        submit(task, errorHandler, lockKey, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Runnable errorHandler, String lockKey, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        LOG.trace("Submitting task with lock file [{}], timeout [{}] expressed in unit [{}]", lockKey, timeout, timeUnit);

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task, lockKey);

        SetMDCContextTaskRunnable setMDCContextTaskRunnable = new SetMDCContextTaskRunnable(synchronizedRunnable, errorHandler);
        final ClearDomainRunnable clearDomainRunnable = new ClearDomainRunnable(domainContextProvider, setMDCContextTaskRunnable);

        submitRunnable(schedulingTaskExecutor, clearDomainRunnable, errorHandler, waitForTask, timeout, timeUnit);
    }

    @Override
    public void submit(Runnable task, Domain domain) {
        submit(schedulingTaskExecutor, task, domain, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void submit(Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        submit(schedulingTaskExecutor, task, domain, waitForTask, timeout, timeUnit);
    }

    @Override
    public void submitLongRunningTask(Runnable task, Domain domain) {
        submitLongRunningTask(task, null, domain);
    }

    @Override
    public void submitLongRunningTask(Runnable task, Runnable errorHandler, Domain domain) {
        submit(schedulingLongTaskExecutor, new SetMDCContextTaskRunnable(task, errorHandler), domain, false, null, null);
    }

    protected Future<?> submit(SchedulingTaskExecutor taskExecutor, Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        LOG.trace("Submitting task for domain [{}]", domain);

        final DomainRunnable domainRunnable = new DomainRunnable(domainContextProvider, domain, task);
        Future<?> utrFuture = submitRunnable(taskExecutor, domainRunnable, waitForTask, timeout, timeUnit);

        LOG.trace("Completed task for domain [{}]", domain);

        return utrFuture;
    }

    protected Future<?> submitRunnable(SchedulingTaskExecutor taskExecutor, Runnable task, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        return submitRunnable(taskExecutor, task, null, waitForTask, timeout, timeUnit);
    }

    protected Future<?> submitRunnable(SchedulingTaskExecutor taskExecutor, Runnable task, Runnable errorHandler, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        final Future<?> utrFuture = taskExecutor.submit(task);

        if (waitForTask) {
            LOG.debug("Waiting for task to complete");
            try {
                utrFuture.get(timeout, timeUnit);
                LOG.debug("Task completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleRunnableError(e, errorHandler);
            } catch (ExecutionException | TimeoutException e) {
                handleRunnableError(e, errorHandler);
            }
        }
        return utrFuture;
    }

    protected void handleRunnableError(Exception exception, Runnable errorHandler) {
        if (errorHandler != null) {
            LOG.debug("Running the error handler");
            errorHandler.run();
            return;
        }

        throw new DomainTaskException("Could not execute task", exception);
    }
}
