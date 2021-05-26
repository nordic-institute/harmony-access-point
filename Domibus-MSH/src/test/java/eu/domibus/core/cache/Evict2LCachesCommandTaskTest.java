package eu.domibus.core.cache;

import eu.domibus.api.cluster.Command;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class Evict2LCachesCommandTaskTest {

    @Tested
    private Evict2LCachesCommandTask evict2LCachesCommandTask;

    @Injectable
    protected DomibusCacheService domibusCacheService;

    @Test
    public void canHandle() {
        assertTrue(evict2LCachesCommandTask.canHandle(Command.EVICT_2LC_CACHES));
    }

    @Test
    public void canHandleWithDifferentCommand() {
        assertFalse(evict2LCachesCommandTask.canHandle("anothercommand"));
    }

    @Test
    public void execute(@Injectable Map<String, String> properties) {
        evict2LCachesCommandTask.execute(properties);

        new FullVerifications() {{
            domibusCacheService.clear2LCCaches();
        }};
    }
}