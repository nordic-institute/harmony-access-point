package eu.domibus.api.cache;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public interface CacheService {

    /**
     * Clear all caches from the cacheManager
     */
    void evictCaches();

}
