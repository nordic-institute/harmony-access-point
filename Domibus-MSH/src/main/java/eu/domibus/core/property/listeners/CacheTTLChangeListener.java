package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy;
import eu.domibus.logging.DomibusLogger;
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

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CACHE_DDC_LOOKUP;
import static eu.domibus.core.cache.DomibusCacheConfiguration.CACHE_MANAGER;
import static eu.domibus.core.cache.DomibusCacheService.DYNAMIC_DISCOVERY_ENDPOINT;

/**
 * Class enables dynamic update of the cache TTL for ehcache 3.x provider for the properties:
 *  - property: domibus.cache.ddc.lookup.ttl and cache configuration:lookupInfo
 *
 * For dynamic change to work, the cache expiry policy must be configured with class DomibusCacheDynamicExpiryPolicy
 *
 * <pre>
 * &lt;cache alias="lookupInfo">
 *   &lt;expiry>
 *     <class>eu.domibus.core.cache.DomibusCacheDynamicExpiryPolicy</class>
 *   &lt;/expiry>
 * &lt;/cache>
 *</pre>
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Service
public class CacheTTLChangeListener implements DomibusPropertyChangeListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CacheTTLChangeListener.class);

    final CacheManager cacheManager;
    final Map<String, String> propertyCacheMapping;


    public CacheTTLChangeListener(@Qualifier(value = CACHE_MANAGER) CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        // initialize property to cache mapping
        propertyCacheMapping = new HashMap<>();
        propertyCacheMapping.put(DOMIBUS_CACHE_DDC_LOOKUP,DYNAMIC_DISCOVERY_ENDPOINT );
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsAnyIgnoreCase(propertyName, DOMIBUS_CACHE_DDC_LOOKUP);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("Change cache ttl for property:[{}] to value [{}]!", propertyName, propertyValue);
        if (!propertyCacheMapping.containsKey(propertyName)){
            LOG.error("Property: [{}] is not supported!", propertyName);
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        String cacheKey = propertyCacheMapping.get(propertyName);

        JCacheCacheManager jCacheManager = (JCacheCacheManager) cacheManager;
        javax.cache.CacheManager cacheManager = jCacheManager.getCacheManager();
        javax.cache.Cache cache = cacheManager.getCache(cacheKey);
        Eh107Configuration<Long, String> eh107Configuration = (Eh107Configuration) cache.getConfiguration(Eh107Configuration.class);
        CacheRuntimeConfiguration<Long, String> runtimeConfiguration = eh107Configuration.unwrap(CacheRuntimeConfiguration.class);

        if (!(runtimeConfiguration.getExpiryPolicy() instanceof DomibusCacheDynamicExpiryPolicy)){
            LOG.error("Can not update TTL for cache [{}] and property: [{}], because cache is not configured with DomibusCacheDynamicExpiryPolicy!", cacheKey, propertyName);
            throw new IllegalArgumentException("Cache: [" + cacheKey+"] is not configured with DomibusCacheDynamicExpiryPolicy! Property ["+propertyName+"] can not be updated!");
        }

        DomibusCacheDynamicExpiryPolicy expiryPolicy = (DomibusCacheDynamicExpiryPolicy) runtimeConfiguration.getExpiryPolicy();
        expiryPolicy.setTTLInSeconds(Long.parseLong(propertyValue));
        LOG.info("TTL for cache [{}] for property: [{}] was updated to [{}] seconds!", cacheKey, propertyName, propertyValue);
    }
}
