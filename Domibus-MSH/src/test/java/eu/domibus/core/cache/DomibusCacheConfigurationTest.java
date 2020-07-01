package eu.domibus.core.cache;

import mockit.*;
import mockit.integration.junit4.JMockit;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DomibusCacheConfigurationTest {

    @Tested
    DomibusCacheConfiguration domibusCacheConfiguration;

    @Injectable
    CacheManager cacheManager;

    @Test
    public void testEhCacheManagerWithExternalFilePresent() {
        new Expectations(domibusCacheConfiguration) {{
            domibusCacheConfiguration.externalCacheFileExists();
            result = true;

            domibusCacheConfiguration.mergeExternalCacheConfiguration(cacheManager);
        }};

        domibusCacheConfiguration.ehCacheManager(cacheManager);

        new FullVerifications(domibusCacheConfiguration) {{
            domibusCacheConfiguration.addPluginsCacheConfiguration(cacheManager, anyString);
        }};
    }

    @Test
    public void testEhCacheManagerWithNoExternalFilePresent() {
        new Expectations(domibusCacheConfiguration) {{
            domibusCacheConfiguration.externalCacheFileExists();
            result = false;
        }};

        domibusCacheConfiguration.ehCacheManager(cacheManager);

        new FullVerifications(domibusCacheConfiguration) {{
            domibusCacheConfiguration.addPluginsCacheConfiguration(cacheManager, anyString);
        }};
    }

    @Test
    public void test_mergeExternalCacheConfiguration(@Mocked CacheManager defaultCacheManager,
                                                     @Mocked CacheManager externalCacheManager) {
        new Expectations(domibusCacheConfiguration) {{
            domibusCacheConfiguration.createCacheManager(anyString, anyString);
            result = externalCacheManager;
        }};

        domibusCacheConfiguration.mergeExternalCacheConfiguration(defaultCacheManager);

        new FullVerifications(domibusCacheConfiguration) {{
            domibusCacheConfiguration.overridesDefaultCache(defaultCacheManager, externalCacheManager);
        }};
    }

    @Test
    public void test_overridesDefaultCache(@Mocked CacheManager defaultCacheManager,
                                           @Mocked CacheManager externalCacheManager,
                                           @Mocked CacheConfiguration cacheConfiguration
    ) {
        new Expectations() {{
            externalCacheManager.getCacheNames();
            result = new String[]{"cache1", "cache2"};

            defaultCacheManager.cacheExists("cache1");
            result = true;

            externalCacheManager.getCache(anyString).getCacheConfiguration();
            result = cacheConfiguration;
        }};

        domibusCacheConfiguration.overridesDefaultCache(defaultCacheManager, externalCacheManager);

        new Verifications() {{
            defaultCacheManager.removeCache("cache1");

            List<Cache> cacheParams = new ArrayList<>();
            defaultCacheManager.addCache(withCapture(cacheParams));
            Assert.assertTrue(cacheParams.size() == 2);
        }};
    }

}
