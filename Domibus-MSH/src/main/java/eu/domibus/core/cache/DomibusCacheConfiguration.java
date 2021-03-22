package eu.domibus.core.cache;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @author Catalin Enache
 * @since 4.0
 */
@Configuration
public class DomibusCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheConfiguration.class);

    @Value("${domibus.config.location}/internal/ehcache.xml")
    protected String externalEhCacheFile;

    @Value("${domibus.config.location}/plugins/config")
    protected String pluginsConfigLocation;

    @Bean(name = "cacheManager")
    public EhCacheCacheManager ehCacheManager(@Autowired CacheManager cacheManager) {
        EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
        ehCacheManager.setCacheManager(cacheManager);

        if (externalCacheFileExists()) {
            mergeExternalCacheConfiguration(cacheManager);
        }

        addPluginsCacheConfiguration(cacheManager, pluginsConfigLocation);

        return ehCacheManager;
    }


    @Bean(name = "ehcache")
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        final ClassPathResource classPathResource = new ClassPathResource("config/ehcache/ehcache-default.xml");
        ehCacheManagerFactoryBean.setConfigLocation(classPathResource);
        ehCacheManagerFactoryBean.setCacheManagerName("default");
        return ehCacheManagerFactoryBean;
    }

    protected boolean externalCacheFileExists() {
        return new File(externalEhCacheFile).exists();
    }

    /**
     * Get the configuration defined in the external ehcache.xml file and merge it into the default ehcache-default.xml configuration.
     * Any existing cache entry is overridden, otherwise a new cache entry is created.
     *
     * @param cacheManager
     */
    protected void mergeExternalCacheConfiguration(CacheManager cacheManager) {
        LOG.debug("External ehCache file exists [{}]. Overriding the default ehCache configuration", externalEhCacheFile);

        CacheManager externalCacheManager = createCacheManager(externalEhCacheFile, "external");

        overridesDefaultCache(cacheManager, externalCacheManager);
    }

    /**
     * Adds plugins ehcache config files (both default-ehcache.xml and ehcache.xml)
     *
     * @param cacheManager Domibus core cache Manager
     * @param pluginsConfigLocation plugins config location
     */
    protected void addPluginsCacheConfiguration(CacheManager cacheManager, final String pluginsConfigLocation) {
        List<Resource> pluginDefaultEhcacheList = readPluginEhcacheFiles("classpath*:config/ehcache/*-plugin-default-ehcache.xml");
        List<Resource> pluginEhcacheList = readPluginEhcacheFiles("file:///" + pluginsConfigLocation + "/*-plugin-ehcache.xml");
        if (CollectionUtils.isEmpty(pluginDefaultEhcacheList) && CollectionUtils.isEmpty(pluginEhcacheList)) {
            LOG.debug("no ehcache config files found for plugins");
            return;
        }

        CacheManager cacheManagerPlugins = CacheManager.create();

        //default ehcache files
        pluginDefaultEhcacheList.forEach(resource -> readPluginCacheConfig(cacheManagerPlugins, resource));

        //ehcache files
        pluginEhcacheList.forEach(resource -> readPluginCacheConfig(cacheManagerPlugins, resource));

        //add to Domibus cache
        final String[] cacheNames = cacheManagerPlugins.getCacheNames();
        if (ArrayUtils.isEmpty(cacheNames)) {
            LOG.debug("no ehcache caches will be merged for plugins");
            return;
        }
        for (String cacheName : cacheNames) {
            if (cacheManager.cacheExists(cacheName)) {
                final String errorMessage = "Plugin cache \"" + cacheName + "\" already exists in Domibus";
                throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, errorMessage);
            }
            final CacheConfiguration cacheConfiguration = cacheManagerPlugins.getCache(cacheName).getCacheConfiguration();
            cacheManager.addCache(new Cache(cacheConfiguration));
        }
    }

    /**
     * Retrieves the location of default-ehcache.xml and ehcache.xml files
     *
     * @param locationPattern
     * @return {@code List<Resource>}
     */
    protected List<Resource> readPluginEhcacheFiles(String locationPattern) {
        List<Resource> resourceList = new ArrayList<>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            resourceList = Arrays.asList(resolver.getResources(locationPattern));
        } catch (IOException e) {
            LOG.error("Error while reading [{}] files", locationPattern, e);
        }
        return resourceList;
    }

    /**
     * Reads the plugin caches defined in either *plugin-default-ehcache.xml either *plugin-ehcache.xml file
     *
     * @param cacheManager Domibus core cacheManager
     * @param resource     new Resource file
     */
    protected void readPluginCacheConfig(@NotNull CacheManager cacheManager, @NotNull Resource resource) {
        final String configurationFileName = resource.getFilename();
        if (configurationFileName == null) {
            LOG.info("unable to add plugin ehcache config from [{}]", resource);
            return;
        }
        LOG.debug("Adding the following plugin ehcache file [{}]", configurationFileName);
        try {
            CacheManager pluginCacheManager = createCacheManager(resource.getURL(), configurationFileName);
            overridesDefaultCache(cacheManager, pluginCacheManager);
        } catch (CacheException | IOException e) {
            LOG.error("Error while loading cache from [{}]", configurationFileName, e);
        }
    }

    /**
     * Overrides the caches found in defaultCacheManager by the caches from cacheManager
     *
     * @param defaultCacheManager
     * @param cacheManager
     */
    protected void overridesDefaultCache(@NotNull CacheManager defaultCacheManager, @NotNull CacheManager cacheManager) {
        final String[] cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            if (defaultCacheManager.cacheExists(cacheName)) {
                LOG.debug("Overriding the default cache [{}]", cacheName);
                defaultCacheManager.removeCache(cacheName);
            }
            final CacheConfiguration cacheConfiguration = cacheManager.getCache(cacheName).getCacheConfiguration();
            defaultCacheManager.addCache(new Cache(cacheConfiguration));
        }
    }

    protected CacheManager createCacheManager(@NotNull URL configurationURL, @NotNull final String configurationName) {
        net.sf.ehcache.config.Configuration configuration = ConfigurationFactory.parseConfiguration(configurationURL);
        configuration.setName(configurationName);
        return CacheManager.newInstance(configuration);
    }

    protected CacheManager createCacheManager(@NotNull String configurationFileName, @NotNull String configurationName) {
        net.sf.ehcache.config.Configuration configuration = ConfigurationFactory.parseConfiguration(new File(configurationFileName));
        configuration.setName(configurationName);
        return CacheManager.newInstance(configuration);
    }

}
