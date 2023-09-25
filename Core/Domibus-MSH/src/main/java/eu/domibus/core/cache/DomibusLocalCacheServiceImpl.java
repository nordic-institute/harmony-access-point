package eu.domibus.core.cache;

import eu.domibus.api.cache.DomibusCacheException;
import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class DomibusLocalCacheServiceImpl implements DomibusLocalCacheService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusLocalCacheServiceImpl.class);

    protected CacheManager cacheManager;

    protected List<DomibusCacheServiceNotifier> domibusCacheServiceNotifierList;

    protected LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    public DomibusLocalCacheServiceImpl(CacheManager cacheManager,
                                        @Lazy List<DomibusCacheServiceNotifier> domibusCacheServiceNotifierList /*Lazy injection to avoid cyclic dependency as we are dynamically injecting all listeners */,
                                        @Lazy LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean/*Lazy injection to avoid cyclic dependency as the entityManagerFactory needs DomibusMultiTenantConnectionProvider*/) {
        this.cacheManager = cacheManager;
        this.domibusCacheServiceNotifierList = domibusCacheServiceNotifierList;
        this.localContainerEntityManagerFactoryBean = localContainerEntityManagerFactoryBean;
    }

    @Override
    public void clearCache(String cacheName) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            return;
        }
        LOG.debug("Clearing cache [{}]", cacheName);
        cache.clear();
    }

    @Override
    public void evict(String cacheName, String propertyName) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            return;
        }
        LOG.debug("Evicting property [{}] of cache [{}]", propertyName, cacheName);
        cache.evict(propertyName);
    }

    @Override
    public void clearAllCaches(boolean notification) throws DomibusCoreException {
        LOG.debug("Clearing all caches from the cacheManager");
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).clear();
        }
        if (notification) {
            notifyClearAllCaches();
        }
    }

    @Override
    public List<String> getCacheNames() {
        List<String> result = new ArrayList<>();
        final Collection<String> cacheNames = cacheManager.getCacheNames();
        if (CollectionUtils.isNotEmpty(cacheNames)) {
            result.addAll(cacheNames);
        }
        return result;


    }

    @Override
    public void clear2LCCaches(boolean notification) throws DomibusCoreException {
        SessionFactory sessionFactory = localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getCache().evictAllRegions();
        if (notification) {
            notifyClear2LCaches();
        }
    }

    private Cache getCacheByName(String name) {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            if (StringUtils.equalsIgnoreCase(cacheName, name)) {
                return cacheManager.getCache(cacheName);
            }
        }
        LOG.warn("Could not find cache with name cache [{}]", name);
        return null;
    }

    @Override
    public boolean containsCacheForKey(String key, String cacheName) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            LOG.debug("Cache with name [{}] does not exist!", cacheName);
            return false;
        }
        return ((javax.cache.Cache) cache.getNativeCache()).containsKey(key);
    }

    @Override
    public Object getEntryFromCache(String cacheName, String key) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            throw new DomibusCacheException("Cannot get entry [" + key + "] from cache [" + cacheName + "]. Cache does not exists");
        }
        LOG.debug("Getting entry [{}] from cache [{}]", key, cacheName);
        final Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper != null) {
            return valueWrapper.get();
        }
        return null;
    }

    @Override
    public void addEntryInCache(String cacheName, String key, Object value) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            throw new DomibusCacheException("Cannot add entry [" + key + "] in cache [" + cacheName + "]. Cache does not exists");
        }
        LOG.debug("Adding entry [{}] with value [{}] to cache [{}]", key, value, cacheName);
        cache.put(key, value);
    }

    @Override
    public void evictEntryFromCache(String cacheName, String key) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            throw new DomibusCacheException("Cannot remove entry [" + key + "] from cache [" + cacheName + "]. Cache does not exists");
        }
        LOG.debug("Evicting entry [{}] from cache [{}]", key, cacheName);
        cache.evict(key);
    }

    @Override
    public Map<String, Object> getEntriesFromCache(String cacheName) {
        final Cache cache = getCacheByName(cacheName);
        if (cache == null) {
            throw new DomibusCacheException("Cannot get entries from cache [" + cacheName + "]. Cache does not exists");
        }
        Map<String, Object> result = new HashMap<>();

        final Iterator iterator = ((javax.cache.Cache) cache.getNativeCache()).iterator();
        while (iterator.hasNext()) {
            javax.cache.Cache.Entry<String, Object> next = (javax.cache.Cache.Entry<String, Object>) iterator.next();
            final String key = next.getKey();
            final Object value = next.getValue();
            result.put(key, value);
        }
        return result;
    }

    protected void notifyClearAllCaches() {
        LOG.debug("Notifying cache subscribers about clear all caches event");
        domibusCacheServiceNotifierList
                .forEach(DomibusCacheServiceNotifier::notifyClearAllCaches);
    }

    protected void notifyClear2LCaches() {
        LOG.debug("Notifying cache subscribers about clear second level caches event");
        domibusCacheServiceNotifierList
                .forEach(DomibusCacheServiceNotifier::notifyClear2LCaches);
    }
}
