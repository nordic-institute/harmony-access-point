package eu.domibus.core.cache.distributed.configuration;

import com.hazelcast.config.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Cosmin Baciu
 * @since 5.1
 */
@Service
public class DomibusDistributedCacheConfigurationHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDistributedCacheConfigurationHelper.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    public DomibusDistributedCacheConfigurationHelper(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public MapConfig createMapConfig(String mapName, final Integer mapSize, final Integer timeToLiveSeconds, final Integer maxIdle, NearCacheConfig nearCacheConfig) {
        MapConfig mapConfig = new MapConfig(mapName);
        mapConfig.setBackupCount(0);

        mapConfig.addEntryListenerConfig(new EntryListenerConfig().setIncludeValue(true).setImplementation(new HazelcastMapEntryListener()));

        LOG.info("Setting TTL for distributed cache [{}] to [{}]", mapName, timeToLiveSeconds);
        mapConfig.setTimeToLiveSeconds(timeToLiveSeconds);

        LOG.info("Setting max idle for distributed cache [{}] to [{}]", mapName, maxIdle);
        mapConfig.setMaxIdleSeconds(maxIdle);

        final EvictionConfig evictionConfig = mapConfig.getEvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);

        LOG.info("Setting size for distributed cache [{}] to [{}]", mapName, mapSize);
        evictionConfig.setSize(mapSize);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_SIZE);
        mapConfig.setEvictionConfig(evictionConfig);

        mapConfig.setNearCacheConfig(nearCacheConfig);

        return mapConfig;
    }

    public NearCacheConfig createDefaultMapNearCacheConfig() {
        final Integer defaultSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_SIZE);
        final Integer defaultTtl = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_TTL);
        final Integer maxIdle = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_NEAR_CACHE_DEFAULT_MAX_IDLE);
        return createMapNearCacheConfig(defaultSize, defaultTtl, maxIdle);
    }

    public NearCacheConfig createMapNearCacheConfig(final Integer mapSize, final Integer timeToLiveSeconds, final Integer maxIdle) {
        LOG.info("Setting size for distributed near cache to [{}]", mapSize);

        EvictionConfig evictionConfig = new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setSize(mapSize);

        LOG.info("Setting TTL to [{}] and max idle to [{}] for distributed near cache ", timeToLiveSeconds, maxIdle);

        NearCacheConfig nearCacheConfig = new NearCacheConfig()
                .setCacheLocalEntries(true)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setInvalidateOnChange(true)
                .setTimeToLiveSeconds(timeToLiveSeconds)
                .setMaxIdleSeconds(maxIdle)
                .setEvictionConfig(evictionConfig);

        return nearCacheConfig;
    }




}
