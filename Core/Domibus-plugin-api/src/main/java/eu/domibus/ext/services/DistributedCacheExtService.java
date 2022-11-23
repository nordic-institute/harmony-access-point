package eu.domibus.ext.services;

import eu.domibus.ext.exceptions.CacheExtServiceException;

/**
 * Service responsible for managing the distributed cache. Changes are propagated automatically amongst the cluster members.
 * The service fallbacks to local cache in a non cluster environment.
 *
 * @author Cosmin Baciu
 * @since 5.1
 */
public interface DistributedCacheExtService {

    /**
     * Creates or gets a distributed cache with the specified name.
     * If the cache does not exist, it will be created with the default values and near cache configuration specified in domibus-default.properties
     * @param cacheName The name of the cache
     */
    void createCache(String cacheName);

    /**
     * Creates or gets a distributed cache with the specified name and configuration.
     * If the cache does not exist, it will be created with the specified configuration and near cache configuration specified in domibus-default.properties
     * @param cacheName The name of the cache
     * @param cacheSize The max cache size
     * @param timeToLiveSeconds The time to live in seconds for the cache entries
     * @param maxIdleSeconds Maximum number of seconds for each entry to stay idle in the cache.
     */
    void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds);

    /**
     * Creates or gets a distributed cache with the specified name and configuration.
     * If the cache does not exist, it will be created with the specified configuration and specified near cache configuration
     * @param cacheName The name of the cache
     * @param cacheSize The max cache size
     * @param timeToLiveSeconds The time to live in seconds for the cache entries
     * @param maxIdleSeconds Maximum number of seconds for each entry to stay idle in the cache.
     * @param nearCacheSize The near cache default size for the distributed cache
     * @param nearCacheTimeToLiveSeconds The near cache maximum number of seconds for each entry to stay in the near cache
     * @param nearCacheMaxIdleSeconds The near cache maximum number of seconds for each entry to stay idle in the cache.
     * @throws CacheExtServiceException in case the cache does not exist
     */
    void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds);

    /**
     * Adds an entry in the cache
     * @param cacheName The cache name in which we add entries
     * @param key The key of the entry
     * @param value The value to add in the cache
     * @throws CacheExtServiceException if the cache does not exist
     */
    void addEntryInCache(String cacheName, String key, Object value) throws CacheExtServiceException;

    /**
     * Gets an entry from the cache
     * @param cacheName The cache name from which we get entries
     * @param key The key of the entry
     * @throws CacheExtServiceException if the cache does not exist
     */
    Object getEntryFromCache(String cacheName, String key) throws CacheExtServiceException;

    /**
     * Evicts an entry from the cache
     * @param cacheName The cache name in from which we want we evict entries
     * @param key The key of the entry
     * @throws CacheExtServiceException if the cache does not exist
     */
    void evictEntryFromCache(String cacheName, String key) throws CacheExtServiceException;
}
