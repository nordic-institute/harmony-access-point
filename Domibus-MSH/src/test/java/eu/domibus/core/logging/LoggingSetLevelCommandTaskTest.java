package eu.domibus.core.logging;

import eu.domibus.api.cluster.CommandProperty;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class LoggingSetLevelCommandTaskTest {

    @Tested
    LoggingSetLevelCommandTask loggingSetLevelCommandTask;

    @Injectable
    protected LoggingService loggingService;

    @Test
    public void canHandle() {
    }

    @Test
    public void execute() {
        final Map<String, String> commandProperties = new HashMap<>();
        final String level = "DEBUG";
        final String name = "eu.domibus";
        commandProperties.put(CommandProperty.LOG_LEVEL, level);
        commandProperties.put(CommandProperty.LOG_NAME, name);

        loggingSetLevelCommandTask.execute(commandProperties);

        new Verifications() {{
            final String nameActual, levelActual;
            loggingService.setLoggingLevel(nameActual = withCapture(), levelActual = withCapture());
            Assert.assertEquals(level, levelActual);
            Assert.assertEquals(name, nameActual);
        }};


    }
}