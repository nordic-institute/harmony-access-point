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
public class EvictCachesCommandTaskTest {

    @Tested
    private EvictCachesCommandTask evictCachesCommandTask;

    @Injectable
    protected DomibusCacheService domibusCacheService;

    @Test
    public void canHandle() {
        assertTrue(evictCachesCommandTask.canHandle(Command.EVICT_CACHES));
    }

    @Test
    public void canHandleWithDifferentCommand() {
        assertFalse(evictCachesCommandTask.canHandle("anothercommand"));
    }

    @Test
    public void execute(@Injectable Map<String, String> properties) {

        evictCachesCommandTask.execute(properties);

        new FullVerifications() {{
            domibusCacheService.clearAllCaches(false);
        }};
    }
}