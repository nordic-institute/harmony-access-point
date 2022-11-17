package eu.domibus.core.cache.distributed;

import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author Cosmin Baciu
 * @since 5.1
 */
public class HazelcastClusterMembershipListener implements MembershipListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HazelcastClusterMembershipListener.class);

    public void memberAdded(MembershipEvent membershipEvent) {
        LOG.info("Distributed cache cluster member added [{}]", membershipEvent);
    }

    public void memberRemoved(MembershipEvent membershipEvent) {
        LOG.info("Distributed cache cluster member removed [{}]", membershipEvent);
    }
}