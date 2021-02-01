package eu.domibus.ext.delegate.services.cache;

import eu.domibus.ext.services.CacheExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceDelegate implements CacheExtService {

    @Autowired
    eu.domibus.api.cache.CacheService cacheService;

    @Override
    public void evictCaches() {
        cacheService.evictCaches();
    }

}
