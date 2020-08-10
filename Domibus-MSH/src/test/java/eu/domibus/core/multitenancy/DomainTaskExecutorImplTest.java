package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.ClearDomainRunnable;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.SchedulingTaskExecutor;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void submit(@Injectable Runnable task,
                       @Injectable Runnable errorHandler,
                       @Injectable File file) {
        domainTaskExecutor.submit(task, errorHandler, file);

        new Verifications() {{
            domainTaskExecutor.submitRunnable(taskExecutor, (ClearDomainRunnable) any, true, DEFAULT_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
        }};
    }
}