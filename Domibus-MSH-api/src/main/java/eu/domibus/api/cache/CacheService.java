package eu.domibus.api.cache;

public interface CacheService {

    /**
     * Deletes a {@code Party}
     * @throws
     */
    void evictCaches();

}
