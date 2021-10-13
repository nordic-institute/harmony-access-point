package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.ClearDomainRunnable;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.multitenancy.lock.SynchronizedRunnableFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.SchedulingTaskExecutor;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.domibus.core.multitenancy.DomainTaskExecutorImpl.DEFAULT_WAIT_TIMEOUT_IN_SECONDS;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomainTaskExecutorImplTest {

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected SchedulingTaskExecutor taskExecutor;

    @Injectable
    protected SchedulingTaskExecutor quartzTaskExecutor;

    @Injectable
    SynchronizedRunnableFactory synchronizedRunnableFactory;

    @Tested
    DomainTaskExecutorImpl domainTaskExecutor;

    @Test
    public void testSubmitRunnable(@Injectable Runnable submitRunnable) {
        domainTaskExecutor.submitRunnable(taskExecutor, submitRunnable, false, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        new Verifications() {{
            taskExecutor.submit(submitRunnable);
        }};
    }

    @Test(expected = DomainTaskException.class)
    public void testSubmitRunnableThreadInterruption(@Injectable Runnable submitRunnable,
                                                     @Injectable Future<?> utrFuture) throws Exception {
        new Expectations() {{
            taskExecutor.submit(submitRunnable);
            result = utrFuture;

            utrFuture.get(anyLong, withAny(TimeUnit.SECONDS));
            result = new InterruptedException();
        }};

        domainTaskExecutor.submitRunnable(taskExecutor, submitRunnable, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        new Verifications() {{
            taskExecutor.submit(submitRunnable);
            times = 1;
        }};
    }

    @Test
    public void testSubmitRunnableThreadWithTimeoutExceeded(@Injectable Runnable submitRunnable,
                                                            @Injectable Runnable errorHandler,
                                                            @Injectable Future<?> utrFuture) throws Exception {
        new Expectations() {{
            taskExecutor.submit(submitRunnable);
            result = utrFuture;

            utrFuture.get(anyLong, withAny(TimeUnit.SECONDS));
            result = new TimeoutException();
        }};

        domainTaskExecutor.submitRunnable(taskExecutor, submitRunnable, errorHandler, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);

        new Verifications() {{
            taskExecutor.submit(submitRunnable);
            times = 1;

            errorHandler.run();
            times = 1;
        }};
    }

}
