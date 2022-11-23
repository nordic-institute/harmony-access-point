package eu.domibus.core.cache.distributed;

import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import eu.domibus.api.cache.DomibusCacheException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.distributed.configuration.DomibusDistributedCacheConfigurationHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

public class DistributedCacheDao {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DistributedCacheDao.class);

    protected HazelcastInstance hazelcastInstance;
    protected DomibusDistributedCacheConfigurationHelper distributedCacheConfigurationHelper;

    protected DomibusPropertyProvider domibusPropertyProvider;

    public DistributedCacheDao(HazelcastInstance hazelcastInstance,//when running in a non cluster environment, the hazelcastInstance is not even created
                               DomibusPropertyProvider domibusPropertyProvider,
                               DomibusDistributedCacheConfigurationHelper distributedCacheConfigurationHelper) {
        this.hazelcastInstance = hazelcastInstance;
        this.distributedCacheConfigurationHelper = distributedCacheConfigurationHelper;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public IMap<String, Object> createCache(String name) {
        LOGGER.debug("Creating or getting cache [{}]", name);

        final Integer cacheSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_DEFAULT_SIZE);
        final Integer timeToLiveSeconds = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_DEFAULT_TTL);
        final Integer maxIdleSeconds = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_MAX_IDLE);

        final Integer nearCacheSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_SIZE);
        final Integer nearCacheTimeToLiveSeconds = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_TTL);
        final Integer nearCacheMaxIdleSeconds = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_MAX_IDLE);

        return doGetCache(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
    }

    public IMap<String, Object> createCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds) {
        LOGGER.debug("Creating or getting cache [{}]", name);
        return doGetCache(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, 0, 0, 0);
    }


    public IMap<String, Object> createCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds) {
        LOGGER.debug("Creating or getting cache [{}]", name);
        return doGetCache(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdleSeconds);
    }


    protected IMap<String, Object> doGetCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, final Integer nearCacheMaxIdle) {
        createMapConfigIfNeeded(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdle);
        LOGGER.trace("Getting cache [{}]", name);
        IMap<String, Object> mapObject = hazelcastInstance.getMap(name);
        return mapObject;
    }

    protected void createMapConfigIfNeeded(String cacheName, Integer cacheSize, Integer timeToLiveSeconds, Integer maxIdleSeconds, Integer nearCacheSize, Integer nearCacheTimeToLiveSeconds, final Integer nearCacheMaxIdle) {
        if (cacheExists(cacheName)) {
            LOGGER.trace("Map config [{}] already exists", cacheName);
            return;
        }

        LOGGER.info("Creating map config for [{}] with cacheSize [{}], timeToLiveSeconds [{}], maxIdleSeconds [{}], nearCacheSize [{}], nearCacheTimeToLiveSeconds [{}], nearCacheMaxIdle [{}]",
                cacheName, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdle);

        NearCacheConfig nearCacheConfig = null;
        if (nearCacheSize > 0 && nearCacheTimeToLiveSeconds > 0) {
            LOGGER.info("Setting near cache config nearCacheSize [{}], nearCacheTimeToLiveSeconds [{}], nearCacheMaxIdle [{}]", nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdle);
            nearCacheConfig = distributedCacheConfigurationHelper.createMapNearCacheConfig(nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdle);
        }
        final MapConfig newMapConfig = distributedCacheConfigurationHelper.createMapConfig(cacheName, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheConfig);
        addMapConfig(cacheName, newMapConfig);
    }

    private void addMapConfig(String cacheName, MapConfig newMapConfig) {
        try {
            hazelcastInstance.getConfig().addMapConfig(newMapConfig);
        } catch (InvalidConfigurationException e) {
            LOGGER.warn("Cannot add new map config: map configuration [{}] already exists", cacheName);
            LOGGER.trace("Cannot add new map config: map configuration [{}] already exists", cacheName, e);
        }
    }

    protected boolean cacheExists(String cacheName) {
        MapConfig mapConfig = hazelcastInstance.getConfig().getMapConfigOrNull(cacheName);
        return mapConfig != null && StringUtils.equals(cacheName, mapConfig.getName());
    }

    public void addEntryInCache(String cacheName, String key, Object value) {
        validateCacheExists(cacheName);
        final IMap<String, Object> cache = createCache(cacheName);
        cache.set(key, value);
        LOGGER.info("Added key [{}] with value [{}] in cache [{}]", key, value, cacheName);
    }

    public Object getEntryFromCache(String cacheName, String key) {
        validateCacheExists(cacheName);
        final IMap<String, Object> cache = createCache(cacheName);
        LOGGER.debug("Getting entry [{}] from cache [{}]", key, cacheName);
        return cache.get(key);
    }


    public void removeEntryFromCache(String cacheName, String key) {
        validateCacheExists(cacheName);

        final IMap<String, Object> cache = createCache(cacheName);
        cache.delete(key);
        LOGGER.info("Removed key [{}] from cache [{}]", key, cacheName);
    }

    private void validateCacheExists(String cacheName) {
        final boolean exists = cacheExists(cacheName);
        if (!exists) {
            throw new DomibusCacheException("Cache [" + cacheName + "] does not exists. Please create it before doing operations on cache.");
        }
    }
}
