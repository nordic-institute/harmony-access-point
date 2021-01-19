package eu.domibus.core.cache;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since  5.0
 */
@Service
public class ClusterCacheServiceNotifier implements DomibusCacheServiceNotifier {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClusterCacheServiceNotifier.class);

    protected SignalService signalService;

    public ClusterCacheServiceNotifier(SignalService signalService) {
        this.signalService = signalService;
    }

    @Override
    public void notifyClearAllCaches() {
        LOG.debug("Received notification to clear all caches");

        signalService.signalClearCaches();
    }
}
