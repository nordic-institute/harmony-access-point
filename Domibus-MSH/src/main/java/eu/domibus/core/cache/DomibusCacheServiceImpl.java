package eu.domibus.core.cache;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class DomibusCacheServiceImpl implements DomibusCacheService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    @Lazy
    protected SignalService signalService;

    @Override
    public void clearCache(String refreshCacheName) {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            if (StringUtils.equalsIgnoreCase(cacheName, refreshCacheName)) {
                final Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    LOG.debug("Clearing cache [{}]", refreshCacheName);
                    cache.clear();
                }
            }
        }
    }

    @Override
    public void clearAllCaches(){
        LOG.debug("clearing all caches from the cacheManager");
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).clear();
        }
        signalService.signalClearCaches();
    }
}
