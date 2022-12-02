package eu.domibus.core.cache.distributed;

import com.hazelcast.core.HazelcastInstance;
import eu.domibus.AbstractIT;
import eu.domibus.api.cache.distributed.DistributedCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@TestPropertySource(properties = {"domibus.deployment.clustered=false"})
public class DistributedCacheServiceNonClusterTestIT extends AbstractIT {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DistributedCacheServiceNonClusterTestIT.class);
    public static final String EXISTING_LOCAL_CACHE = "userDomain";

    @Autowired
    DistributedCacheService distributedCacheService;

    @Autowired(required = false)
    HazelcastInstance hazelcastInstance;

    @Test
    public void testCreateCache() {
        //check that in a non cluster deployment, the create cache operations are not failing
        distributedCacheService.createCache("mycache1");
        distributedCacheService.createCache("mycache2", 1, 2, 3);
        distributedCacheService.createCache("mycache3", 1, 1, 1, 1, 1, 1);

        //in a non cluster deployment, distributed cache is not created
        assertNull(hazelcastInstance);
    }

    @Test
    public void evictEntryFromCache() {
        final String myKey = "mykey";
        final String myValue = "myvalue";
        distributedCacheService.addEntryInCache(EXISTING_LOCAL_CACHE, myKey, myValue);

        assertEquals(myValue, distributedCacheService.getEntryFromCache(EXISTING_LOCAL_CACHE, myKey));
        distributedCacheService.evictEntryFromCache(EXISTING_LOCAL_CACHE, myKey);
        assertNull(distributedCacheService.getEntryFromCache(EXISTING_LOCAL_CACHE, myKey));
    }

    @Test
    public void getCacheNames() {
        final List<String> distributedCacheNames = distributedCacheService.getDistributedCacheNames();
        assertNotNull(distributedCacheNames);
    }

    @Test
    public void getEntriesFromCache() {
        distributedCacheService.createCache(EXISTING_LOCAL_CACHE);
        distributedCacheService.addEntryInCache(EXISTING_LOCAL_CACHE, "key11", "value11");
        final Map<String, Object> entriesFromCache = distributedCacheService.getEntriesFromCache(EXISTING_LOCAL_CACHE);
        assertNotNull(entriesFromCache);
        assertEquals(1, entriesFromCache.size());
        assertEquals("value11", entriesFromCache.get("key11"));
    }
}
