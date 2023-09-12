package eu.domibus.core.multitenancy.lock;

import eu.domibus.api.multitenancy.lock.DomibusSynchronizationException;
import eu.domibus.api.multitenancy.lock.SynchronizationService;
import eu.domibus.api.multitenancy.lock.DbClusterSynchronizedRunnableFactory;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes a task with a lock (either a db lock for cluster deployment or a simple java lock otherwise)
 *
 * @author Ion Perpegel
 * @since 5.2
 */
@Service
public class SynchronizationServiceImpl implements SynchronizationService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizationServiceImpl.class);

    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    private final DomibusConfigurationService domibusConfigurationService;

    private final DbClusterSynchronizedRunnableFactory dbClusterSynchronizedRunnableFactory;

    public SynchronizationServiceImpl(DomibusConfigurationService domibusConfigurationService,
                                      DbClusterSynchronizedRunnableFactory dbClusterSynchronizedRunnableFactory) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.dbClusterSynchronizedRunnableFactory = dbClusterSynchronizedRunnableFactory;
    }

    @Override
    public <T> Callable<T> getSynchronizedCallable(Callable<T> task, String dbLockKey, String javaLockKey) {
        Callable<T> synchronizedRunnable;
        if (domibusConfigurationService.isClusterDeployment()) {
            synchronizedRunnable = dbClusterSynchronizedRunnableFactory.synchronizedCallable(task, dbLockKey);
        } else {
            synchronizedRunnable = javaSyncCallable(task, javaLockKey);
        }
        return synchronizedRunnable;
    }

    @Override
    public <T> T execute(Callable<T> task, String dbLockKey, String javaLockKey) {
        Callable<T> synchronizedRunnable = getSynchronizedCallable(task, dbLockKey, javaLockKey);
        try {
            return synchronizedRunnable.call();
        } catch (Exception e) {
            throw new DomibusSynchronizationException(e);
        }
    }

    @Override
    public void execute(Runnable task, String dbLockKey, String javaLockKey) {
        Callable<Boolean> synchronizedRunnable = getSynchronizedCallable(() -> {
            task.run();
            return true;
        }, dbLockKey, javaLockKey);
        try {
            synchronizedRunnable.call();
        } catch (DomibusSynchronizationException se) {
            throw se;
        } catch (Exception e) {
            throw new DomibusSynchronizationException("Error executing a task with locks:" + dbLockKey + ", " + javaLockKey, e);
        }
    }

    @Override
    public void execute(Runnable task, String dbLockKey) {
        execute(task, dbLockKey, null);
    }

    private <T> Callable<T> javaSyncCallable(Callable<T> task, String javaLockKey) {
        return () -> {
            if (javaLockKey != null) {
                synchronized (locks.computeIfAbsent(javaLockKey, k -> new Object())) {
                    return executeTask(task);
                }
            } else {
                return executeTask(task);
            }
        };
    }

    private <T> T executeTask(Callable<T> task) {
        try {
            LOG.debug("Handling sync execution with java lock.");
            T res = task.call();
            LOG.debug("Finished handling sync execution with java lock.");
            return res;
        } catch (Exception e) {
            throw new DomibusSynchronizationException("Error executing a callable task with java lock.", e);
        }
    }
}
