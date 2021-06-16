package eu.domibus.core.cache;

import com.google.common.collect.Lists;
import eu.domibus.api.cluster.SignalService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DomibusCacheServiceImplTest {

    @Tested
    private DomibusCacheServiceImpl domibusCacheService;

    @Injectable
    private CacheManager cacheManager;

    @Injectable
    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

    @Mocked
    private Cache cache;

    @Injectable
    SignalService signalService;

    @Injectable
    List<DomibusCacheServiceNotifier> domibusCacheServiceNotifiers;

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsNoCaches() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.<String>newArrayList();
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsOnlyNonMatchingCaches() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("nonMatching");
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsNullMatchingCache() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("cache");
            cacheManager.getCache("cache"); result = null;
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void refreshTheCacheWhenTheCacheManagerContainsMatchingCache() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("cache");
            cacheManager.getCache("cache"); result = cache;
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 1;
        }};
    }

    @Test
    public void clearAllCaches(@Injectable DomibusCacheServiceNotifier domibusCacheServiceNotifier) {
        Collection<String> cacheNames = new ArrayList<>();
        String cacheName = "cache1";
        cacheNames.add(cacheName);

        new Expectations(domibusCacheService) {{
            cacheManager.getCacheNames();
            result = cacheNames;

            domibusCacheService.notifyClearAllCaches();
        }};

        domibusCacheService.clearAllCaches();

        new Verifications() {{
            cacheManager.getCache(cacheName).clear();
            times = 1;
        }};
    }

    @Test
    public void clear2LCaches(@Injectable DomibusCacheServiceNotifier domibusCacheServiceNotifier, @Injectable SessionFactory sessionFactory) {

        new Expectations(domibusCacheService) {{
            localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().unwrap(SessionFactory.class);
            result = sessionFactory;

            domibusCacheService.notifyClear2LCaches();
        }};

        domibusCacheService.clear2LCCaches();

        new Verifications() {{
            sessionFactory.getCache().evictAll();
            times = 1;
        }};
    }
}