package eu.domibus.core;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnable;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnableFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static eu.domibus.core.spring.DomibusContextRefreshedListener.SYNC_LOCK_KEY;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Ignore("[EDELIVERY-8739] Improve code coverage")
@Transactional
public class SynchronizedRunnableIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(SynchronizedRunnableIT.class);

    @Autowired
    SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Test
    @Transactional
    public void blocksWithTimeout() {
        AtomicInteger i = new AtomicInteger();
        Runnable task1 = () -> {
            LOG.info("Task 1 enter");
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 1 exit");
        };

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 2 exit");
            Assert.fail();
        };
        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, SYNC_LOCK_KEY);
        Thread t2 = new Thread(synchronizedRunnable2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
        LOG.info("Launch task 2");
        t2.start();

        try {
            t2.join();
            Assert.assertEquals(1, i.get());
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
    }

    @Test
    @Transactional
    public void allTasksSucceed() {
        AtomicInteger i = new AtomicInteger();
        Runnable task1 = () -> {
            LOG.info("Task 1 enter");
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 1 exit");
        };

        SynchronizedRunnable synchronizedRunnable = synchronizedRunnableFactory.synchronizedRunnable(task1, SYNC_LOCK_KEY);
        Thread t1 = new Thread(synchronizedRunnable);
        t1.start();

        Runnable task2 = () -> {
            LOG.info("Task 2 enter");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("SynchronizedRunnableIT stopped", e);
            }
            i.getAndIncrement();
            LOG.info("Task 2 exit");
        };
        SynchronizedRunnable synchronizedRunnable2 = synchronizedRunnableFactory.synchronizedRunnable(task2, SYNC_LOCK_KEY);
        Thread t2 = new Thread(synchronizedRunnable2);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
        LOG.info("Launch task 2");
        t2.start();

        try {
            t2.join();
            Assert.assertEquals(2, i.get());
        } catch (InterruptedException e) {
            LOG.error("SynchronizedRunnableIT stopped", e);
        }
    }

}
