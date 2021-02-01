package eu.domibus.core.cache;

import eu.domibus.api.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;

public class CacheDefaultService implements CacheService {

    @Autowired
    DomibusCacheService domibusCacheService;
    /**
     * Deletes a {@code Party}

     */
    @Override
    public void evictCaches() {
       domibusCacheService.clearAllCaches();
    }
}
