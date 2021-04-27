package eu.domibus.logging;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author François Gautier
 * @since 5.0
 * <p>
 * Test the performance of async log of logback
 * <p>
 * use logback-test.xml in resources
 */
public class LogbackAsyncPerfTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LogbackAsyncPerfTest.class);

    public static final int COUNT = 5000;

    /**
     * Run 3 threads producing 10 000 log lines each (5 000 warn and 5 000 info).
     * In the case of queue overflow, the info line would be dropped.
     * <p>
     * generates a log file in target/log to check if no lines have been drop with current configuration.
     * With config:
     * <queueSize>1024</queueSize>
     * <discardingThreshold>0</discardingThreshold>
     * <p>
     * COUNT = 5 000
     * <p>
     * result: **** [30000 lines of log] [1.145 s] in file + console -> no log lost
     */
    @Test
    public void latch() throws InterruptedException {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService taskExecutor = Executors.newFixedThreadPool(4);
        List<Runnable> tasks = Arrays.asList(
                getRunnable(" -   ", latch),
                getRunnable(" --  ", latch),
                getRunnable(" --- ", latch));

        for (Runnable task : tasks) {
            taskExecutor.execute(task);
        }

        // latch.await() somehow didn't work and JUNIT would end the thread prematurely
        while (latch.getCount() != 0) {
            Thread.sleep(100);
        }

        long end = System.currentTimeMillis();
        LOG.error("\n**** [{}] [{} s]", tasks.size() * COUNT * 2, BigDecimal.valueOf(end - start).divide(BigDecimal.valueOf(1000), 3, RoundingMode.DOWN));
    }

    private Runnable getRunnable(String s, CountDownLatch latch) {
        return () -> printLog(s, latch);
    }

    private void printLog(String title, CountDownLatch latch) {
        for (int i = 1; i < COUNT + 1; i++) {
            LOG.info("info  [{} {}]", title, i);
            LOG.warn("warn  [{} {}]", title, i);
        }
        if (latch != null) {
            latch.countDown();
        }
    }

}
