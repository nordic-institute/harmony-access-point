package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        addPluginsCacheConfigurationClasspath(cacheManager);

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

    protected void addPluginsCacheConfigurationClasspath(CacheManager cacheManager) {
        List<Resource> pluginDefaultEhcacheList = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            pluginDefaultEhcacheList = Arrays.asList(resolver.getResources("classpath*:config/ehcache/*-plugin-default-ehcache.xml"));
        } catch (IOException e) {
            LOG.error("Error while reading *-plugin-default-ehcache.xml files", e);
        }
        if (CollectionUtils.isEmpty(pluginDefaultEhcacheList)) {
            LOG.debug("no *-plugin-default-ehcache.xml files found");
            return;
        }

        pluginDefaultEhcacheList.forEach(resource -> readCacheConfigCheckIfExists(cacheManager, resource));
    }

    /**
     *
     * @param cacheManager Domibus core cacheManager
     * @param resource new Resource file
     */
    protected void readCacheConfigCheckIfExists(@NotNull CacheManager cacheManager, @NotNull Resource resource) {
        final String fileName = resource.getFilename();
        LOG.debug("Adding the following plugin default ehcache file [{}]", fileName);
        try {
            net.sf.ehcache.config.Configuration configuration = ConfigurationFactory.parseConfiguration(resource.getURL());
            configuration.setName(fileName);
            CacheManager pluginCacheManager = CacheManager.newInstance(configuration);
            final String[] cacheNames = pluginCacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                if (cacheManager.cacheExists(cacheName)) {
                    final String errorMessage = "Plugin cache " + cacheName + " declared in " + fileName + " already exists in Domibus";
                    LOG.error(errorMessage);
                    throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
                }
                final CacheConfiguration cacheConfiguration = pluginCacheManager.getCache(cacheName).getCacheConfiguration();
                cacheManager.addCache(new Cache(cacheConfiguration));
            }
        } catch (CacheException | IOException e) {
            LOG.error("Error while loading cache from [{}]", fileName, e);
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
