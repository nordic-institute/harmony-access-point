package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.lock.SynchronizationService;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnable;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnableFactory;
import eu.domibus.core.multitenancy.DomainTaskExecutorImpl;
import eu.domibus.core.spring.lock.LockDao;
import eu.domibus.core.spring.lock.LockEntity;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DomainTaskExecutorImplIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainTaskExecutorImplIT.class);

    @Autowired
    DomainTaskExecutorImpl domainTaskExecutor;

    @Autowired
    SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Test
    @Transactional
    public void test2() {
        Runnable task1 = () -> {
            LOG.info("Task 1 enter.");
            try {
                Thread.sleep(7000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.info("Task 1 exit");
        };

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.createBean(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Task 2 exit");
        };
        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.createBean(task2, SYNC_LOCK_KEY);
        Thread t2 = new Thread(synchronizedRunnable2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t2.start();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Transactional
    public void test1() {

        Runnable task1 = () -> {
            LOG.info("Task 1 enter.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Executing task1.");
            LOG.info("Task 1 exit.");
        };
        Runnable errorHandler1 = () -> {
            LOG.warn("An error has occurred in task1");
        };

        Thread t1 = new Thread(() -> domainTaskExecutor.submit(task1, errorHandler1, SYNC_LOCK_KEY, true, 100L, TimeUnit.SECONDS));
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LOG.info("Executing task2");
            LOG.info("Task 2 exit.");
        };
        Runnable errorHandler2 = () -> {
            LOG.warn("An error has occurred in task2");
        };

        Thread t2 = new Thread(() -> domainTaskExecutor.submit(task2, errorHandler2, SYNC_LOCK_KEY, true, 100L, TimeUnit.SECONDS));
        t2.start();
    }

}
