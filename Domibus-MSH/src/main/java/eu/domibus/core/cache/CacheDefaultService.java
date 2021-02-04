package eu.domibus.core.cache;

import eu.domibus.api.cache.CacheService;
import org.springframework.stereotype.Service;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@Service
public class CacheDefaultService implements CacheService {

    private final DomibusCacheService domibusCacheService;

    public CacheDefaultService(DomibusCacheService domibusCacheService) {
        this.domibusCacheService = domibusCacheService;
    }

    /**
     * Clearing all caches from the cacheManager
     */
    @Override
    public void evictCaches() {
        domibusCacheService.clearAllCaches();
    }
}
