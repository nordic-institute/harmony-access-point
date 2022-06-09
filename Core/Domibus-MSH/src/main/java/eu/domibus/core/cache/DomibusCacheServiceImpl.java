package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class DomibusCacheServiceImpl implements DomibusCacheService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);

    protected CacheManager cacheManager;

    protected List<DomibusCacheServiceNotifier> domibusCacheServiceNotifierList;

    protected LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    public DomibusCacheServiceImpl(CacheManager cacheManager,
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
    public void clear2LCCaches(boolean notification) throws DomibusCoreException {
        SessionFactory sessionFactory = localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getCache().evictAll();
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
        return ((javax.cache.Cache)cache.getNativeCache()).containsKey(key);
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
