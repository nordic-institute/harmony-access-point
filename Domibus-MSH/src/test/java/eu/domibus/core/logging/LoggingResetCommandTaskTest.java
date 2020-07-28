package eu.domibus.core.logging;

import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class LoggingResetCommandTaskTest {

    @Tested
    LoggingResetCommandTask loggingResetCommandTask;

    @Injectable
    protected LoggingService loggingService;

    @Test
    public void canHandle() {
    }

    @Test
    public void execute() {
        loggingResetCommandTask.execute(null);

        new Verifications() {{
            loggingService.resetLogging();
        }};
    }
}