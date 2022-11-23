package eu.domibus.core.cache.distributed;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.map.impl.proxy.MapProxyImpl;
import com.hazelcast.map.impl.proxy.NearCachedMapProxyImpl;
import eu.domibus.AbstractIT;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.Assert.*;

@TestPropertySource(properties = {"domibus.deployment.clustered=true"})
public class DistributedCacheServiceImplTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DistributedCacheServiceImplTestIT.class);
    public static final int EXPECTED_CACHE_SIZE = 5000;
    public static final int EXPECTED_TTL = 3600;
    public static final int EXPECTED_MAX_IDLE = 3600;

    @Autowired
    DistributedCacheServiceImpl distributedCacheService;

    @Test
    public void testGetCacheWithDefaultConfiguration() {
        final Map<String, Object> mycache = distributedCacheService.getCache("mycache");
        final MapConfig mapConfig = ((NearCachedMapProxyImpl) (mycache)).getMapConfig();
        assertEquals(EXPECTED_CACHE_SIZE, mapConfig.getEvictionConfig().getSize());
        assertEquals(0, mapConfig.getBackupCount());
        assertEquals(EXPECTED_TTL, mapConfig.getTimeToLiveSeconds());
        assertEquals(EXPECTED_MAX_IDLE, mapConfig.getMaxIdleSeconds());

        final NearCacheConfig nearCacheConfig = mapConfig.getNearCacheConfig();
        assertEquals(EXPECTED_CACHE_SIZE, nearCacheConfig.getEvictionConfig().getSize());
        assertEquals(EXPECTED_TTL, nearCacheConfig.getTimeToLiveSeconds());
    }

    @Test
    public void testGetCacheWithCustomConfigurationAndDefaultNearCache() {
        final int cacheSize = 10;
        final int timeToLiveSeconds = 60;
        final int maxIdleSeconds = 100;
        final Map<String, Object> myCache = distributedCacheService.getCache("myCustomCache", cacheSize, timeToLiveSeconds, maxIdleSeconds);
        final MapConfig mapConfig = ((MapProxyImpl) (myCache)).getMapConfig();
        assertEquals(cacheSize, mapConfig.getEvictionConfig().getSize());
        assertEquals(0, mapConfig.getBackupCount());
        assertEquals(timeToLiveSeconds, mapConfig.getTimeToLiveSeconds());
        assertEquals(maxIdleSeconds, mapConfig.getMaxIdleSeconds());

        final NearCacheConfig nearCacheConfig = mapConfig.getNearCacheConfig();
        assertNull(nearCacheConfig);
    }

    @Test
    public void testGetCacheWithCustomConfigurationAndCustomNearCache() {
        final int cacheSize = 10;
        final int timeToLiveSeconds = 60;
        final int maxIdleSeconds = 100;
        final int nearCacheSize = 2000;
        final int nearCacheTimeToLiveSeconds = 120;
        final int nearCacheMaxIdleSeconds = 50;
        final Map<String, Object> myCache = distributedCacheService.getCache("myCustomCache1", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
        final MapConfig mapConfig = ((NearCachedMapProxyImpl) (myCache)).getMapConfig();
        assertEquals(cacheSize, mapConfig.getEvictionConfig().getSize());
        assertEquals(0, mapConfig.getBackupCount());
        assertEquals(timeToLiveSeconds, mapConfig.getTimeToLiveSeconds());
        assertEquals(maxIdleSeconds, mapConfig.getMaxIdleSeconds());

        final NearCacheConfig nearCacheConfig = mapConfig.getNearCacheConfig();
        assertEquals(nearCacheSize, nearCacheConfig.getEvictionConfig().getSize());
        assertEquals(nearCacheTimeToLiveSeconds, nearCacheConfig.getTimeToLiveSeconds());
        assertEquals(nearCacheMaxIdleSeconds, nearCacheConfig.getMaxIdleSeconds());

        final Map<String, Object> myCache1 = distributedCacheService.getCache("myCustomCache1", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
        //check that we retrieve the same cache and not a newly created cache
        assertTrue(myCache == myCache1);
    }
}
