package eu.domibus.core.property.listeners;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DYNAMICDISCOVERY_LOOKUP_CACHE_TTL;
import static eu.domibus.core.cache.DomibusCacheConfiguration.CACHE_MANAGER;
import static eu.domibus.core.cache.DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT;

/**
 * Class enables dynamic update of the cache TTL for ehcache 3.x provider for the properties:
 * - property: domibus.dynamicdiscovery.lookup.cache.ttl and cache configuration:lookupInfo
 * <p>
 * For dynamic change to work, the cache expiry policy must be configured with class DomibusCacheDynamicExpiryPolicy
 *
 * <pre>
 * &lt;cache alias="lookupInfo">
 *   &lt;expiry>
 *     <class>eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy</class>
 *   &lt;/expiry>
 * &lt;/cache>
 * </pre>
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Service
public class DynamicDiscoveryCacheTTLChangeListener implements DomibusPropertyChangeListener {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryCacheTTLChangeListener.class);

    protected final CacheManager cacheManager;
    protected final Map<String, String> propertyCacheMapping;
    protected final DomibusPropertyProvider domibusPropertyProvider;


    public DynamicDiscoveryCacheTTLChangeListener(@Qualifier(value = CACHE_MANAGER) CacheManager cacheManager, DomibusPropertyProvider domibusPropertyProvider) {
        this.cacheManager = cacheManager;
        // initialize property to cache mapping
        this.propertyCacheMapping = new HashMap<>();
        this.propertyCacheMapping.put(DOMIBUS_DYNAMICDISCOVERY_LOOKUP_CACHE_TTL, DYNAMIC_DISCOVERY_ENDPOINT);
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_DYNAMICDISCOVERY_LOOKUP_CACHE_TTL);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("Change cache ttl for property:[{}] to value [{}]!", propertyName, propertyValue);
        if (!propertyCacheMapping.containsKey(propertyName)) {
            throw new DomibusCoreException("TTL cache property: [" + propertyName + "] is not supported!");
        }
        //get the TTL value
        Long value = getTtlValue(propertyName, propertyValue);

        String cacheName = propertyCacheMapping.get(propertyName);
        updateCacheTTL(cacheName, value, propertyName);
    }

    private Long getTtlValue(String propertyName, String propertyValue) {
        if (StringUtils.isBlank(propertyValue)) {
            LOG.debug("Because value is empty, set default cache ttl [{}] for property:[{}]!", propertyValue, propertyName, propertyValue);
            return 3600L;
        }
        try {
            return Long.parseLong(propertyValue);
        } catch (NumberFormatException ex) {
            throw new DomibusCoreException("Illegal value [" + propertyValue + "] TTL cache property: [" + propertyName + "]! Value is not a number!");
        }
    }

    private void updateCacheTTL(String cacheName, Long newTtlValue, String propertyName){
        JCacheCacheManager jCacheManager = (JCacheCacheManager) cacheManager;
        javax.cache.CacheManager cacheManager = jCacheManager.getCacheManager();
        javax.cache.Cache cache = cacheManager.getCache(cacheName);
        Eh107Configuration<Long, String> eh107Configuration = (Eh107Configuration) cache.getConfiguration(Eh107Configuration.class);
        CacheRuntimeConfiguration<Long, String> runtimeConfiguration = eh107Configuration.unwrap(CacheRuntimeConfiguration.class);

        if (!(runtimeConfiguration.getExpiryPolicy() instanceof DomibusCacheDynamicExpiryPolicy)){
            throw new DomibusCoreException("Cache: [" + cacheName+"] is not configured with DomibusCacheDynamicExpiryPolicy! Property ["+propertyName+"] can not be updated!");
        }

        DomibusCacheDynamicExpiryPolicy expiryPolicy = (DomibusCacheDynamicExpiryPolicy) runtimeConfiguration.getExpiryPolicy();
        expiryPolicy.resetTTLInSeconds();
        LOG.info("TTL for cache [{}] for property: [{}] was updated to [{}] seconds!", cacheName, propertyName, newTtlValue);

    }
}
