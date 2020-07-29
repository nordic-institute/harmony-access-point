package eu.domibus.core.cache;

import eu.domibus.api.cluster.Command;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Collection;
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
    EvictCachesCommandTask evictCachesCommandTask;

    @Injectable
    protected CacheManager cacheManager;

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
        Collection<String> cacheNames = new ArrayList<>();
        String cacheName = "mycache";
        cacheNames.add(cacheName);

        new Expectations() {{
            cacheManager.getCacheNames();
            result = cacheNames;
        }};


        evictCachesCommandTask.execute(properties);

        new Verifications() {{
            cacheManager.getCache(cacheName).clear();
        }};
    }
}