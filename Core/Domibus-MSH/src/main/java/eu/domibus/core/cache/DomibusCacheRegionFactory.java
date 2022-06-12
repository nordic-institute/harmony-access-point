package eu.domibus.core.cache;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.springframework.core.io.ClassPathResource;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Map;
import java.util.Objects;

/**
 * @author François Gautier
 * @since 5.0
 */
public class DomibusCacheRegionFactory extends JCacheRegionFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheRegionFactory.class);

    private static volatile ClassLoader classLoader;

    protected CacheManager resolveCacheManager(SessionFactoryOptions settings, Map properties) {
        Objects.requireNonNull(classLoader, "Please set Spring's classloader in the setBeanClassLoader " +
                "method before using this class in Hibernate");

        final ClassPathResource classPathResource = new ClassPathResource(DomibusCacheConfiguration.CONFIG_EHCACHE_EHCACHE_DEFAULT_XML);
        CachingProvider provider = Caching.getCachingProvider();

        CacheManager cacheManager;
        try {
            cacheManager = provider.getCacheManager(
                    classPathResource.getURL().toURI(),
                    classLoader);
        } catch (Exception e) {
            LOG.error("Cache manager could not be retrieved with defaultEhCacheFile [{}] and classloader [{}]. Use default cacheManager creation.",
                    DomibusCacheConfiguration.CONFIG_EHCACHE_EHCACHE_DEFAULT_XML,
                    classLoader,
                    e);
            cacheManager = super.resolveCacheManager(settings, properties);
        }

        // To prevent some class loader memory leak this might cause
        setBeanClassLoader(null);

        return cacheManager;
    }

    /**
     * This method must be called from a Spring Bean to get the classloader.
     * For example: BeanClassLoaderAwareJCacheRegionFactory.setBeanClassLoader(this.getClass().getClassLoader());
     *
     * @param classLoader The Spring classloader
     */
    public static void setBeanClassLoader(ClassLoader classLoader) {
        DomibusCacheRegionFactory.classLoader = classLoader;
    }
}
