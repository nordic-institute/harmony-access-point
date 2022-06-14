package eu.domibus.plugin.ws.webservice;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusLoggerImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static eu.domibus.api.multitenancy.DomainService.DEFAULT_DOMAIN;
import static org.junit.Assert.fail;

/* Receiving 20 messages on 2 domains:
    Average execution time with previous logging approach   5.598 seconds
    Average execution time with new logging approach        5.5938 seconds*/
public class LoggingSegregationImpactOnReceive extends ReceiveMessageIT {
    public static final Domain ANOTHER_DOMAIN = new Domain("anotherDomain", "another domain");
    private static final DomibusLogger LOG = new DomibusLoggerImpl(LoggerFactory.getLogger(LoggingSegregationImpactOnReceive.class));

    @Before
    public void beforeTest() throws Exception {
        getDurationForReceivingMessages(5);
        setCurrentDomain(ANOTHER_DOMAIN);
    }

    private void setCurrentDomain(Domain domain) {
        LOG.putMDC(DomibusLogger.MDC_DOMAIN, domain.getCode());
    }

    @Test
    public void test() throws Exception {

        DomibusLoggerFactory.USE_PREVIOUS_LOGGING_APPROACH = true;
        Duration duration = getAverageDurationOverNrIterations();
        System.out.println("Average execution time with previous logging approach " + duration);

        DomibusLoggerFactory.USE_PREVIOUS_LOGGING_APPROACH = false;
        duration = duration = getAverageDurationOverNrIterations();
        System.out.println("Average execution time with new logging approach " + duration);
    }

    private Duration getAverageDurationOverNrIterations() throws Exception {
        int nrMessages = 10;
        int nrIterations = 3;

        Duration totalDuration = Duration.ZERO;
        setCurrentDomain(DEFAULT_DOMAIN);
        for (int i = 0; i < nrIterations; i++) {
            Duration duration = getDurationForReceivingMessages(nrMessages);
            totalDuration = totalDuration.plus(duration);
        }
        setCurrentDomain(ANOTHER_DOMAIN);
        for (int i = 0; i < nrIterations; i++) {
            Duration duration = getDurationForReceivingMessages(nrMessages);
            totalDuration = totalDuration.plus(duration);
        }
        totalDuration = totalDuration.dividedBy(nrIterations*2);
        return totalDuration;
    }

    private Duration getDurationForReceivingMessages(int nrMessages) throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(nrMessages);
        final ReceiveMessageIT tester = this;
        List<Callable<Boolean>> messageHandlers = Collections.nCopies(nrMessages, () -> {
            tester.testReceiveMessage();
            return true;
        });

        Instant beforeExecution = Instant.now();
        List<Future<Boolean>> results = workers.invokeAll(messageHandlers);
        for (Future<?> result : results) {
            try {
                result.get();
            }
            catch (ExecutionException ex) {
                ex.getCause().printStackTrace();
                fail();
            }
        }
        Instant afterExecution = Instant.now();
        return Duration.between(beforeExecution, afterExecution);
    }
}
