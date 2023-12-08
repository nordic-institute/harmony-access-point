package eu.domibus.api.cache;

import eu.domibus.api.exceptions.DomibusCoreException;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface DomibusLocalCacheService {

    String USER_DOMAIN_CACHE = "userDomain";
    String PREFERRED_USER_DOMAIN_CACHE = "preferredUserDomain";
    String DOMAIN_BY_CODE_CACHE = "domainByCode";
    String DOMAIN_BY_SCHEDULER_CACHE = "domainByScheduler";
    String DYNAMIC_DISCOVERY_ENDPOINT = "lookupInfo";
    String DISPATCH_CLIENT = "dispatchClient";
    String CRL_BY_CERT = "crlByCert";
    String CRL_BY_URL = "crlByUrl";
    String DOMIBUS_PROPERTY_CACHE = "domibusProperties";
    String DOMIBUS_PROPERTY_METADATA_CACHE = "domibusPropertyMetadata";

    void clearCache(String refreshCacheName);

    void evict(String cacheName, String propertyName);

    void clearAllCaches(boolean notification) throws DomibusCoreException;

    List<String> getCacheNames();

    void clear2LCCaches(boolean notification) throws DomibusCoreException;

    /**
     * Method validates if cache for key exists in the cache object with given name.
     * @param key:  key for object in cache
     * @param cacheName: name of the cache
     * @return true if the key exists in the cache with the given name. If the cache object with cachename or key does not exist in the object, returns false.
     */
    boolean containsCacheForKey(String key, String cacheName);

    Object getEntryFromCache(String cacheName, String key);


    void addEntryInCache(String cacheName, String key, Object value);

    void evictEntryFromCache(String cacheName, String key);

    Map<String, Object> getEntriesFromCache(String cacheName);
}
