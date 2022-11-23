package eu.domibus.api.cache.distributed;

import java.util.Map;

/**
 * Service responsible for managing the distributed cache
 */
public interface DistributedCacheService {

    /**
     * Gets a distributed cache with the specified name.
     * If the cache does not exist, it will be created with the default values and near cache configuration specified in domibus-default.properties
     * @param name The name of the cache
     * @return The distributed cache
     */
    Map<String, Object> getCache(String name);

    /**
     * Gets a distributed cache with the specified name and configuration.
     * If the cache does not exist, it will be created with the specified configuration and near cache configuration specified in domibus-default.properties
     * @param name The name of the cache
     * @param cacheSize The max cache size
     * @param timeToLiveSeconds The time to live in seconds for the cache entries
     * @param maxIdleSeconds Maximum number of seconds for each entry to stay idle in the cache.
     * @return The distributed cache
     */
    Map<String, Object> getCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds);

    /**
     * Gets a distributed cache with the specified name and configuration.
     * If the cache does not exist, it will be created with the specified configuration and specified near cache configuration
     * @param name The name of the cache
     * @param cacheSize The max cache size
     * @param timeToLiveSeconds The time to live in seconds for the cache entries
     * @param maxIdleSeconds Maximum number of seconds for each entry to stay idle in the cache.
     * @param nearCacheSize The near cache default size for the distributed cache
     * @param nearCacheTimeToLiveSeconds The near cache maximum number of seconds for each entry to stay in the near cache
     * @param nearCacheMaxIdleSeconds The near cache maximum number of seconds for each entry to stay idle in the cache.
     * @return The distributed cache
     */
    Map<String, Object> getCache(String name, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds);
}
