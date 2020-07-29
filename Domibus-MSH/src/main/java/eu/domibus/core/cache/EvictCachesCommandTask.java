package eu.domibus.core.cache;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class EvictCachesCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(EvictCachesCommandTask.class);

    protected CacheManager cacheManager;

    public EvictCachesCommandTask(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.EVICT_CACHES, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Evicting caches command task");

        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            cacheManager.getCache(cacheName).clear();
        }
    }
}
