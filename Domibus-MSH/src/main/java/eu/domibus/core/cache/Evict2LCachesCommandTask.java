package eu.domibus.core.cache;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class Evict2LCachesCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(Evict2LCachesCommandTask.class);

    protected DomibusCacheService domibusCacheService;

    public Evict2LCachesCommandTask(DomibusCacheService domibusCacheService) {
        this.domibusCacheService = domibusCacheService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.EVICT_2LC_CACHES, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Evicting 2nd level caches command task");

        domibusCacheService.clear2LCCaches();
    }
}
