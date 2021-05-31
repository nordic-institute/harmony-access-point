package eu.domibus.core.cache;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
@EnableCaching
public class DomibusCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheConfiguration.class);

    @Value("${domibus.config.location}/internal/ehcache.xml")
    protected String externalEhCacheFile;

    protected String defaultEhCacheFile = "config/ehcache/ehcache-default.xml";

    @Bean(name = "cacheManager")
    public org.springframework.cache.CacheManager cacheManager() throws Exception {
        CachingProvider provider = Caching.getCachingProvider();

        //default cache
        final ClassPathResource classPathResource = new ClassPathResource(defaultEhCacheFile);

        CacheManager cacheManager = provider.getCacheManager(
                classPathResource.getURL().toURI(),
                getClass().getClassLoader());

        //external cache file
        if (externalCacheFileExists()) {
            mergeExternalCacheConfiguration(provider, cacheManager);
        }

        return new JCacheCacheManager(cacheManager);
    }

    protected boolean externalCacheFileExists() {
        return new File(externalEhCacheFile).exists();
    }

    /**
     * Get the configuration defined in the external ehcache.xml file and merge it into the default ehcache-default.xml configuration.
     * An existing cache entry is overridden, otherwise a new cache entry is created.
     *
     * @param cacheManager
     */
    protected void mergeExternalCacheConfiguration(CachingProvider provider, CacheManager cacheManager) {
        LOG.debug("External ehCache file exists [{}]. Overriding the default ehCache configuration", externalEhCacheFile);

        //external cache file
        CacheManager cacheManagerExternal = provider.getCacheManager(
                new File(externalEhCacheFile).toURI(),
                getClass().getClassLoader());

        overridesDefaultCache(cacheManager, cacheManagerExternal);
    }

    /**
     * Overrides the caches found in defaultCacheManager by the caches from cacheManager
     *
     * @param defaultCacheManager
     * @param cacheManager
     */
    protected void overridesDefaultCache(@NotNull CacheManager defaultCacheManager, @NotNull CacheManager cacheManager) {
        for (String cacheName : cacheManager.getCacheNames()) {
            if (cacheExists(defaultCacheManager, cacheName)) {
                LOG.debug("Overriding the default cache [{}]", cacheName);
                defaultCacheManager.destroyCache(cacheName);
            }

            Cache cache = cacheManager.getCache(cacheName);
            javax.cache.configuration.Configuration config = cache.getConfiguration(javax.cache.configuration.Configuration.class);
            defaultCacheManager.createCache(cacheName, config);
            LOG.debug("Adding [{}] into the default cache", cacheName);
        }
    }

    protected boolean cacheExists(CacheManager cacheManager, String cacheName) {
        List<String> cacheNames =
                StreamSupport.stream(cacheManager.getCacheNames().spliterator(), false)
                        .collect(Collectors.toList());

        return cacheNames.contains(cacheName);
    }
}
