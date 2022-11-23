package eu.domibus.core.cache;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class EvictCachesCommandTask implements CommandTask {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EvictCachesCommandTask.class);

    protected DomibusLocalCacheService domibusLocalCacheService;

    public EvictCachesCommandTask(DomibusLocalCacheService domibusLocalCacheService) {
        this.domibusLocalCacheService = domibusLocalCacheService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.EVICT_CACHES, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOG.info("Evicting caches command task");

        domibusLocalCacheService.clearAllCaches(false);
    }
}
