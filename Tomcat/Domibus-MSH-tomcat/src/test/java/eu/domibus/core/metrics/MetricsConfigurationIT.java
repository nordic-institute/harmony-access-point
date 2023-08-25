package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import eu.domibus.AbstractIT;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

public class MetricsConfigurationIT extends AbstractIT {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MetricsConfigurationIT.class);

    private Thread threadExecutingMethodToTime;
    @Autowired
    private MetricRegistry metricRegistry;

    private static class SomeRuntimeException extends RuntimeException {
    }

    private static class SomeException extends Exception {
    }

    @Test
    public void testMetricRegistryWithoutException() {
        Timer timer = metricRegistry.timer(name("timer"));
        Snapshot snapshotOne = timer.getSnapshot();

        Timer.Context context = timer.time();
        methodToTime();
        context.stop();

        Snapshot snapshotTwo = timer.getSnapshot();
        MatcherAssert.assertThat("Expected to register time when the method to time doesn't throw any exceptions", snapshotTwo.getValues().length, greaterThan(snapshotOne.getValues().length));
        assertNotEquals(snapshotTwo, snapshotOne);
    }

    @Test
    public void testMetricRegistryWithException() {
        Timer timer = metricRegistry.timer(name("timer"));
        long countOne = timer.getCount();

        try {
            Timer.Context context = timer.time();
            methodToTimeThrowingException();
            context.stop();
            fail();
        } catch (Throwable t) {
            LOG.debug("Caught exception as expected", t);
        }

        long countTwo = timer.getCount();
        MatcherAssert.assertThat("Expected to not register time if the method to time throws an exception", countTwo, equalTo(countOne));
    }

    @Test
    public void testMetricRegistryRunnableWithException() {
        Timer timer = metricRegistry.timer(name("timer"));
        long countOne = timer.getCount();
        Thread currentThread = Thread.currentThread();

        try {
            Runnable runnable = this::methodToTimeThrowingException;
            timer.time(runnable);
            fail("Runtime exception from runnable should propagate");
        } catch (SomeRuntimeException e) {
        } catch (Exception e) {
            fail("Expected SomeRuntimeException");
        }

        long countTwo = timer.getCount();
        MatcherAssert.assertThat("Expected to register time even if the method to time throws an exception", countTwo, greaterThan(countOne));
        assertEquals("The method to time should not be executed in a new thread", currentThread, threadExecutingMethodToTime);
    }

    @Test
    public void testMetricRegistryRunnableWithCheckedException() {
        Timer timer = metricRegistry.timer(name("timer"));
        long countOne = timer.getCount();
        Thread currentThread = Thread.currentThread();

        try {
            timer.time(() -> {
                throw new SomeException();
            });
            fail("Exception from Callable should propagate");
        } catch (SomeException e) {
        } catch (Exception e) {
            fail("Expected SomeException");
        }

        long countTwo = timer.getCount();
        MatcherAssert.assertThat("Expected to register time even if the method to time throws an exception", countTwo, greaterThan(countOne));
    }

    private void methodToTimeThrowingException() {
        threadExecutingMethodToTime = Thread.currentThread();
        throw new SomeRuntimeException();
    }

    private void methodToTime() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOG.warn("Exception during sleep", e);
        }
    }
}