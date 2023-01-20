package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author François Gautier
 * @version 5.1
 */
public class DomainTaskExecutorTestImpl implements DomainTaskExecutor {
    @Override
    public <T> T submit(Callable<T> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T submit(Callable<T> task, Domain domain) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submit(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<?> submit(Runnable task, boolean waitForTask) {
        try {
             task.run();
             return new CompletableFuture();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submit(Runnable task, Runnable errorHandler, String lockKey, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submit(Runnable task, Runnable errorHandler, String lockKey) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submit(Runnable task, Domain domain) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submit(Runnable task, Domain domain, boolean waitForTask, Long timeout, TimeUnit timeUnit) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submitLongRunningTask(Runnable task, Runnable errorHandler, Domain domain) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void submitLongRunningTask(Runnable task, Domain domain) {
        try {
            task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
