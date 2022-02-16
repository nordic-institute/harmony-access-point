package eu.domibus.ext.rest;

import eu.domibus.api.cache.CacheService;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.services.CacheExtService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Delegate external Cache service to core for Testing purposes
 *
 * Counters can be requested to count the number of time teh evict cache had been called.
 *
 * @author François Gautier
 * @since 5.0
 */
@Service
@Primary
public class CacheServiceWithCounters implements CacheExtService {

    protected CacheService cacheService;

    private int evictCachesCounter;

    private int evict2LCachesCounter;

    public CacheServiceWithCounters(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public int getEvictCachesCounter() {
        return evictCachesCounter;
    }

    public void resetEvictCachesCounter() {
        this.evictCachesCounter = 0;
    }

    public int getEvict2LCachesCounter() {
        return evict2LCachesCounter;
    }

    public void resetEvict2LCachesCounter() {
        this.evict2LCachesCounter = 0;
    }

    @Override
    public void evictCaches() throws CacheExtServiceException {
        cacheService.evictCaches();
        evictCachesCounter += 1;
    }

    @Override
    public void evict2LCaches() throws CacheExtServiceException {
        cacheService.evict2LCaches();
        evict2LCachesCounter += 1;
    }


}
