package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.common.JPAConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
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
    public void clearAllCaches() throws DomibusCoreException {
        LOG.debug("Clearing all caches from the cacheManager");
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).clear();
        }

        notifyClearAllCaches();
    }

    @Override
    public void clear2LCCaches() throws DomibusCoreException {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getStatistics().logSummary();
        sessionFactory.getCache().evictAll();
        notifyClear2LCaches();
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
