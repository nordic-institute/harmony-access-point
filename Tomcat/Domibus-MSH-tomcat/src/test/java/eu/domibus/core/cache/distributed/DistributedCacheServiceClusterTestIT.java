package eu.domibus.core.cache.distributed;

import com.hazelcast.core.HazelcastInstance;
import eu.domibus.AbstractIT;
import eu.domibus.api.cache.DomibusCacheException;
import eu.domibus.api.cache.distributed.DistributedCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

@TestPropertySource(properties = {"domibus.deployment.clustered=true"})
public class DistributedCacheServiceClusterTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DistributedCacheServiceClusterTestIT.class);

    @Autowired
    DistributedCacheService distributedCacheService;

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Test
    public void testCreateCache() {
        distributedCacheService.createCache("mycache1");
        distributedCacheService.createCache("mycache2", 1, 2, 3);
        distributedCacheService.createCache("mycache3", 1, 1, 1, 1, 1, 1);

        //in a cluster deployment, distributed cache is created
        assertNotNull(hazelcastInstance);
    }


    @Test
    public void addEntryWhenCacheIsAlreadyExisting() {
        final int cacheSize = 10;
        final int timeToLiveSeconds = 60;
        final int maxIdleSeconds = 100;
        final int nearCacheSize = 2000;
        final int nearCacheTimeToLiveSeconds = 120;
        final int nearCacheMaxIdleSeconds = 50;
        distributedCacheService.createCache("myCustomCache3", cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);

        distributedCacheService.addEntryInCache("myCustomCache3", "mykey", "myvalue");
    }

    @Test
    public void addEntryWhenCacheDoesNotExists() {
        try {
            distributedCacheService.addEntryInCache("myCustomCache1", "mykey", "myvalue");
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
        distributedCacheService.createCache(cacheName);

        final String key = "mykey";
        final String value = "myvalue";
        distributedCacheService.addEntryInCache(cacheName, key, value);
        final Object entryFromCache = distributedCacheService.getEntryFromCache(cacheName, key);
        assertEquals(value, entryFromCache);
        distributedCacheService.evictEntryFromCache(cacheName, key);
        assertNull(distributedCacheService.getEntryFromCache(cacheName, key));
    }
}
