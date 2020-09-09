package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Dss cache.
 */
public class DssCache {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DssCache.class);

    private final Cache cache;

    public DssCache(Cache cache) {
        this.cache = cache;
    }

    public void addToCache(final String key, Boolean valid) {
        LOG.debug("Add certificate chain key to cache:[{}], valid:[{}]", key, valid);
        cache.putIfAbsent(new Element(key, valid));
    }

    public boolean isChainValid(final String key) {
        boolean certificateKeyInCache = cache.get(key) != null;
        LOG.debug("Is certificate chain key in cache:[{}]", certificateKeyInCache);
        return certificateKeyInCache;
    }

    public void clear(){
        LOG.debug("Clearing DSS cache");
        cache.removeAll();
    }
}
