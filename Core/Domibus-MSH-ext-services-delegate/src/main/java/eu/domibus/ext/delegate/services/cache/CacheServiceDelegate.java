package eu.domibus.ext.delegate.services.cache;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.ext.services.CacheExtService;
import org.springframework.stereotype.Service;

/**
 * Delegate external Cache service to core
 *
 * @author Soumya Chandran
 * @since 5.0
 */
@Service
public class CacheServiceDelegate implements CacheExtService {

    protected DomibusLocalCacheService cacheService;

    public CacheServiceDelegate(DomibusLocalCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void evictCaches() throws CacheExtServiceException {
        cacheService.clearAllCaches(true);
    }

    @Override
    public void evict2LCaches() throws CacheExtServiceException {
        cacheService.clear2LCCaches(true);
    }
}
