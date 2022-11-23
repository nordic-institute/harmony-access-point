package eu.domibus.core.cache.distributed;

import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import eu.domibus.api.cache.distributed.DistributedCacheService;
import eu.domibus.core.cache.distributed.configuration.DomibusDistributedCacheConfigurationHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DistributedCacheServiceImpl implements DistributedCacheService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DistributedCacheServiceImpl.class);

    protected HazelcastInstance hazelcastInstance;
    protected DomibusDistributedCacheConfigurationHelper distributedCacheConfigurationHelper;

    public DistributedCacheServiceImpl(HazelcastInstance hazelcastInstance, DomibusDistributedCacheConfigurationHelper distributedCacheConfigurationHelper) {
        this.hazelcastInstance = hazelcastInstance;
        this.distributedCacheConfigurationHelper = distributedCacheConfigurationHelper;
    }

    @Override
    public Map<String, Object> getCache(String name) {
        IMap<String, Object> mapObject = hazelcastInstance.getMap(name);
        return mapObject;
    }

    @Override
    public Map<String, Object> getCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds) {
        return doGetCache(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, 0, 0, 0);
    }


    @Override
    public Map<String, Object> getCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds) {
        return doGetCache(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheMaxIdleSeconds, nearCacheTimeToLiveSeconds);
    }


    protected IMap<String, Object> doGetCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, final Integer nearCacheMaxIdle, int nearCacheTimeToLiveSeconds) {
        createMapConfigIfNeeded(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheMaxIdle, nearCacheTimeToLiveSeconds);
        IMap<String, Object> mapObject = hazelcastInstance.getMap(name);
        return mapObject;
    }

    protected void createMapConfigIfNeeded(String name, Integer cacheSize, Integer timeToLiveSeconds, Integer maxIdleSeconds, Integer nearCacheSize, final Integer nearCacheMaxIdle, Integer nearCacheTimeToLiveSeconds) {
        MapConfig mapConfig = hazelcastInstance.getConfig().getMapConfigOrNull(name);

        if (mapConfig != null && StringUtils.equals(name, mapConfig.getName())) {
            LOGGER.trace("Map config [{}] already exists", name);
            return;
        }
        LOGGER.info("Creating map config for [{}] with cacheSize [{}], timeToLiveSeconds [{}], maxIdleSeconds [{}], nearCacheSize [{}], nearCacheMaxIdle [{}], nearCacheTimeToLiveSeconds [{}]",
                name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheSize, nearCacheMaxIdle, nearCacheTimeToLiveSeconds);

        NearCacheConfig nearCacheConfig = null;
        if (nearCacheSize > 0 && nearCacheTimeToLiveSeconds > 0) {
            LOGGER.info("Setting near cache config nearCacheSize [{}], nearCacheMaxIdle [{}], nearCacheTimeToLiveSeconds [{}]", nearCacheSize, nearCacheMaxIdle, nearCacheTimeToLiveSeconds);
            nearCacheConfig = distributedCacheConfigurationHelper.createMapNearCacheConfig(nearCacheSize, nearCacheTimeToLiveSeconds, nearCacheMaxIdle);
        }
        final MapConfig newMapConfig = distributedCacheConfigurationHelper.createMapConfig(name, cacheSize, timeToLiveSeconds, maxIdleSeconds, nearCacheConfig);

        try {
            hazelcastInstance.getConfig().addMapConfig(newMapConfig);
        } catch (InvalidConfigurationException e) {
            LOGGER.warn("Cannot add new map config: map configuration [{}] already exists", name);
            LOGGER.trace("Cannot add new map config: map configuration [{}] already exists", name, e);
        }
    }

}
