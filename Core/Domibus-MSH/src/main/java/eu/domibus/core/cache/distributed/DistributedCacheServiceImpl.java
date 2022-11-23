package eu.domibus.core.cache.distributed;

import eu.domibus.api.cache.DomibusCacheException;
import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.cache.distributed.DistributedCacheService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

public class DistributedCacheServiceImpl implements DistributedCacheService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DistributedCacheServiceImpl.class);
    public static final String CACHE_NOT_CREATED_IN_NON_CLUSTER_MESSAGE = "Cache not created: assuming that in a non cluster environment the cache is already created in ehcache.xml file";

    protected DistributedCacheDao distributedCacheDao;
    protected DomibusLocalCacheService domibusLocalCacheService;
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public void createCache(String cacheName) {
        if (isClusterDeployment()) {
            distributedCacheDao.createCache(cacheName);
            return;
        }
        LOGGER.info(CACHE_NOT_CREATED_IN_NON_CLUSTER_MESSAGE);
    }

    @Override
    public void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds) {
        if (isClusterDeployment()) {
            LOGGER.debug("Creating cache [{}]", cacheName);
            distributedCacheDao.createCache(cacheName);
            return;
        }
        LOGGER.info(CACHE_NOT_CREATED_IN_NON_CLUSTER_MESSAGE);
    }

    @Override
    public void createCache(String cacheName, int cacheSize, int timeToLiveSeconds, int maxIdleSeconds, int nearCacheSize, int nearCacheTimeToLiveSeconds, int nearCacheMaxIdleSeconds) {
        if (isClusterDeployment()) {
            LOGGER.info("Creating cache [{}]", cacheName);
            distributedCacheDao.createCache(cacheName);
            return;
        }
        LOGGER.info(CACHE_NOT_CREATED_IN_NON_CLUSTER_MESSAGE);
    }

    @Override
    public void addEntryInCache(String cacheName, String key, Object value) {
        if (isClusterDeployment()) {
            distributedCacheDao.addEntryInCache(cacheName, key, value);
            return;
        }
        //for single instance defaults to local cache
        domibusLocalCacheService.addEntryInCache(cacheName, key, value);
    }

    @Override
    public Object getEntryFromCache(String cacheName, String key) throws DomibusCacheException {
        if (isClusterDeployment()) {
            return distributedCacheDao.getEntryFromCache(cacheName, key);
        }
        //for single instance defaults to local cache
        return domibusLocalCacheService.getEntryFromCache(cacheName, key);
    }

    @Override
    public void evictEntryFromCache(String cacheName, String key) {
        if (isClusterDeployment()) {
            distributedCacheDao.removeEntryFromCache(cacheName, key);
            return;
        }
        //for single instance defaults to local cache
        domibusLocalCacheService.evictEntryFromCache(cacheName, key);
    }

    protected boolean isClusterDeployment() {
        return domibusConfigurationService.isClusterDeployment();
    }
}
