package eu.domibus.core.cache;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheConfiguration.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Value("${domibus.config.location}/internal/ehcache.xml")
    protected String externalEhCacheFile;

    @Bean(name = "cacheManager")
    public EhCacheCacheManager ehCacheManager(@Autowired CacheManager cacheManager) {
        EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
        ehCacheManager.setCacheManager(cacheManager);

        if (externalCacheFileExists()) {
            mergeExternalCacheConfiguration(cacheManager);
        }

        return ehCacheManager;
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
    protected void mergeExternalCacheConfiguration(CacheManager cacheManager) {
        LOG.debug("External ehCache file exists [{}]. Overriding the default ehCache configuration", externalEhCacheFile);
        CacheManager externalCacheManager = CacheManager.newInstance(externalEhCacheFile);
        final String[] cacheNames = externalCacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            if (cacheManager.cacheExists(cacheName)) {
                LOG.debug("Overriding the default cache [{}]", cacheName);
                cacheManager.removeCache(cacheName);
            }
            final CacheConfiguration cacheConfiguration = externalCacheManager.getCache(cacheName).getCacheConfiguration();
            cacheManager.addCache(new Cache(cacheConfiguration));
        }
    }

    protected void addPluginsCacheConfigurationClasspath(CacheManager cacheManager)  throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] pluginDefaultEhcacheList = resolver.getResources("classpath*:config/*-plugin-default-ehcache.xml");
        for (Resource resource: pluginDefaultEhcacheList) {
            LOG.debug("Adding the following plugin default ehcache file [{}]", resource);
            CacheManager  pluginCacheManager = CacheManager.newInstance(resource.getInputStream());
            final String[] cacheNames = pluginCacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                if (cacheManager.cacheExists(cacheName)) {
                    LOG.warn("Plugin cache [{}] already exists in Domibus", cacheName);
                    continue;
                }
                final CacheConfiguration cacheConfiguration = pluginCacheManager.getCache(cacheName).getCacheConfiguration();
                cacheManager.addCache(new Cache(cacheConfiguration));
            }
        }
    }


    @Bean(name = "ehcache")
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        final ClassPathResource classPathResource = new ClassPathResource("config/ehcache/ehcache-default.xml");
        ehCacheManagerFactoryBean.setConfigLocation(classPathResource);
        ehCacheManagerFactoryBean.setCacheManagerName("default");
        return ehCacheManagerFactoryBean;
    }

}
