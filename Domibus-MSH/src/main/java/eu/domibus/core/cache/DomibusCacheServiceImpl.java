package eu.domibus.core.cache;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class DomibusCacheServiceImpl implements DomibusCacheService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);

    protected CacheManager cacheManager;
    protected List<DomibusCacheServiceNotifier> domibusCacheServiceNotifierList;

    public DomibusCacheServiceImpl(CacheManager cacheManager,
                                   @Lazy List<DomibusCacheServiceNotifier> domibusCacheServiceNotifierList /*Lazy injection to avoid cyclic dependency as we are dynamically injecting all listeners */) {
        this.cacheManager = cacheManager;
        this.domibusCacheServiceNotifierList = domibusCacheServiceNotifierList;
    }

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
    public void clearAllCaches() {
        LOG.debug("Clearing all caches from the cacheManager");
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).clear();
        }

        notifyClearAllCaches();
    }

    protected void notifyClearAllCaches() {
        LOG.debug("Notifying cache subscribers about clear all caches event");
        domibusCacheServiceNotifierList
                .stream()
                .forEach(domibusCacheServiceNotifier -> domibusCacheServiceNotifier.notifyClearAllCaches());
    }
}
