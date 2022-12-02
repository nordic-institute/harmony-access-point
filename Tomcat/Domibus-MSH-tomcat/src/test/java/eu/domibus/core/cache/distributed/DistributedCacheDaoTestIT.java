package eu.domibus.core.cache.distributed;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.impl.proxy.MapProxyImpl;
import com.hazelcast.map.impl.proxy.NearCachedMapProxyImpl;
import eu.domibus.AbstractIT;
import eu.domibus.api.cache.DomibusCacheException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Ignore//@TestPropertySource marks the Spring context as dirty and recreating the context fails due to ActiveMQ
@TestPropertySource(properties = {"domibus.deployment.clustered=true"})
public class DistributedCacheDaoTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DistributedCacheDaoTestIT.class);
    public static final int EXPECTED_CACHE_SIZE = 5000;
    public static final int EXPECTED_TTL = 3600;
    public static final int EXPECTED_MAX_IDLE = 3600;

    @Autowired
    DistributedCacheDao distributedCacheDao;

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Before
    public void beforeTest() {
        distributedCacheDao.getCacheNames().stream().forEach(mapName -> hazelcastInstance.getMap(mapName).destroy());
    }

    @Test
    public void testGetCacheWithDefaultConfiguration() {
        final Map<String, Object> mycache = distributedCacheDao.createCacheIfNeeded("mycache");
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
        final Map<String, Object> myCache = distributedCacheDao.createCacheIfNeeded("myCustomCache", cacheSize, timeToLiveSeconds, maxIdleSeconds);
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
        final Map<String, Object> myCache = distributedCacheDao.createCacheIfNeeded("myCustomCache2", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
        final MapConfig mapConfig = ((NearCachedMapProxyImpl) (myCache)).getMapConfig();
        assertEquals(cacheSize, mapConfig.getEvictionConfig().getSize());
        assertEquals(0, mapConfig.getBackupCount());
        assertEquals(timeToLiveSeconds, mapConfig.getTimeToLiveSeconds());
        assertEquals(maxIdleSeconds, mapConfig.getMaxIdleSeconds());

        final NearCacheConfig nearCacheConfig = mapConfig.getNearCacheConfig();
        assertEquals(nearCacheSize, nearCacheConfig.getEvictionConfig().getSize());
        assertEquals(nearCacheTimeToLiveSeconds, nearCacheConfig.getTimeToLiveSeconds());
        assertEquals(nearCacheMaxIdleSeconds, nearCacheConfig.getMaxIdleSeconds());

        final Map<String, Object> myCache1 = distributedCacheDao.createCacheIfNeeded("myCustomCache2", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
        //check that we retrieve the same cache and not a newly created cache
        assertTrue(myCache == myCache1);
    }

    @Test
    public void addEntryWhenCacheIsAlreadyExisting() {
        final int cacheSize = 10;
        final int timeToLiveSeconds = 60;
        final int maxIdleSeconds = 100;
        final int nearCacheSize = 2000;
        final int nearCacheTimeToLiveSeconds = 120;
        final int nearCacheMaxIdleSeconds = 50;
        distributedCacheDao.createCacheIfNeeded("myCustomCache3", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);

        distributedCacheDao.addEntryInCache("myCustomCache3", "mykey", "myvalue");
    }

    @Test
    public void addEntryWhenCacheDoesNotExists() {
        try {
            distributedCacheDao.addEntryInCache("myCustomCache1", "mykey", "myvalue");
            fail("Adding to cache should have failed due to not existing cache");
        } catch (DomibusCacheException e) {
            final String message = "Normal exception when adding entries to a cache which was not previously created";
            LOG.info(message);
            LOG.trace(message, e);
        }
    }

    @Test
    public void evictEntryFromCache() {
        final String cacheName = "myCustomCache4";
        distributedCacheDao.createCacheIfNeeded(cacheName);

        final String key = "mykey";
        final String value = "myvalue";
        distributedCacheDao.addEntryInCache(cacheName, key, value);
        final Object entryFromCache = distributedCacheDao.getEntryFromCache(cacheName, key);
        assertEquals(value, entryFromCache);
        distributedCacheDao.removeEntryFromCache(cacheName, key);
        assertNull(distributedCacheDao.getEntryFromCache(cacheName, key));
    }

    @Test
    public void getCacheNames() {
        distributedCacheDao.createCacheIfNeeded("cache1");
        distributedCacheDao.createCacheIfNeeded("cache2");
        distributedCacheDao.createCacheIfNeeded("cache3");

        final List<String> cacheNames = distributedCacheDao.getCacheNames();
        assertNotNull(cacheNames);
        assertEquals(3, cacheNames.size());
        cacheNames.contains("cache1");
        cacheNames.contains("cache2");
        cacheNames.contains("cache3");
    }

    @Test
    public void getEntriesFromCache() {
        final String cacheName = "cache11";
        distributedCacheDao.createCacheIfNeeded(cacheName);
        distributedCacheDao.addEntryInCache(cacheName, "key1", "value1");
        distributedCacheDao.addEntryInCache(cacheName, "key2", "value2");

        final Map<String, Object> entriesFromCache = distributedCacheDao.getEntriesFromCache(cacheName);
        assertNotNull(entriesFromCache);
        assertEquals(2, entriesFromCache.size());
        assertEquals("value1", entriesFromCache.get("key1"));
        assertEquals("value2", entriesFromCache.get("key2"));
    }
}
