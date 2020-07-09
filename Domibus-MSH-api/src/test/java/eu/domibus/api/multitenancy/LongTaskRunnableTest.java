package eu.domibus.api.multitenancy;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
@Ignore //TODO:EDELIVERY-6781
public class LongTaskRunnableTest {

    @Injectable
    protected Runnable runnable;

    @Injectable
    protected Runnable errorHandler;

    @Test
    public void run() {
        new Expectations() {{
            runnable.run();
            result = new DomainTaskException("long running task exception");
        }};

        SetMDCContextTaskRunnable longTaskRunnable = new SetMDCContextTaskRunnable(runnable, errorHandler);
        longTaskRunnable.run();

        new Verifications() {{
            errorHandler.run();
        }};
    }
}