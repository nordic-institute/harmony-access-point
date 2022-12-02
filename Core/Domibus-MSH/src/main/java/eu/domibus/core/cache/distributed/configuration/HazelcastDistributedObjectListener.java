package eu.domibus.core.cache.distributed.configuration;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author Cosmin Baciu
 * @since 5.1
 */
public class HazelcastDistributedObjectListener implements DistributedObjectListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HazelcastDistributedObjectListener.class);

    @Override
    public void distributedObjectCreated(DistributedObjectEvent event) {
        DistributedObject instance = event.getDistributedObject();
        LOG.info("Distributed cache object created [{}]", instance.getName());
    }

    @Override
    public void distributedObjectDestroyed(DistributedObjectEvent event) {
        LOG.info("Distributed cache object destroyed [{}]", event.getObjectName());
    }
}