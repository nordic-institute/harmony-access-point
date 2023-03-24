package eu.domibus.core.metrics;

import com.codahale.metrics.*;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implements {@code MetricSet} for JMSQueues counts
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public class JMSQueuesCountSet implements MetricSet {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSQueuesCountSet.class);

    private JMSManager jmsManager;

    private AuthUtils authUtils;

    private DomainTaskExecutor domainTaskExecutor;

    /**
     * seconds
     */
    private long refreshPeriod;

    private boolean showDLQOnly;

    private ServerInfoService serverInfoService;

    private boolean cluster;

    /**
     *
     * @param jmsManager
     * @param authUtils
     * @param domainTaskExecutor
     * @param refreshPeriod
     * @param showDLQOnly
     * @param serverInfoService
     * @param cluster
     */
    public JMSQueuesCountSet(JMSManager jmsManager, AuthUtils authUtils, DomainTaskExecutor domainTaskExecutor, long refreshPeriod, boolean showDLQOnly, ServerInfoService serverInfoService, boolean cluster) {
        this.jmsManager = jmsManager;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.refreshPeriod = refreshPeriod;
        this.showDLQOnly = showDLQOnly;
        this.serverInfoService = serverInfoService;
        this.cluster = cluster;
    }

    public boolean isNodeReady() {
        if (BooleanUtils.isFalse(cluster)) {
            LOG.trace("This is not a cluster, no need to look up for the destinations.");
            return true;
        }
        List<JMSDestination> jmsDestinations = showDLQOnly ? getQueueNamesDLQ() : getQueuesAuthenticated();
        for (JMSDestination jmsDestination : jmsDestinations) {
           if(queueBelongsToNode(jmsDestination)){
               return true;
           }
        }
        return false;
    }

    private boolean queueBelongsToNode(JMSDestination jmsDestination) {
        if (BooleanUtils.isFalse(cluster)) {
            return true;
        }
        String serverName = serverInfoService.getServerName();
        LOG.debug("Checking if queue:[{}] belongs to node:[{}]", jmsDestination.getName(), serverName);
        return jmsDestination.getName().contains(serverName);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();
        List<JMSDestination> jmsDestinations = showDLQOnly ? getQueueNamesDLQ() : getQueuesAuthenticated();
        List<JMSDestination> filteredDestinations = jmsDestinations.stream().filter(this::queueBelongsToNode).collect(Collectors.toList());
        LOG.debug("Using queues [{}] for metrics with refreshPeriod=[{}]", jmsDestinations, refreshPeriod);
        for (JMSDestination jmsDestination : filteredDestinations) {
            addQueueCountToMetrics(gauges, jmsDestination, refreshPeriod);
        }
        return gauges;
    }

    private void addQueueCountToMetrics(Map<String, Metric> gauges, final JMSDestination jmsDestination, final long refreshPeriod) {
        if (refreshPeriod == 0) {
            //no cached metrics
            gauges.put(MetricRegistry.name(jmsDestination.getName()),
                    (Gauge<Long>) () -> getQueueSize(jmsDestination));
        } else {
            gauges.put(MetricRegistry.name(jmsDestination.getName()),
                    new CachedGauge<Long>(refreshPeriod, TimeUnit.SECONDS) {
                        @Override
                        protected Long loadValue() {
                            return getQueueSize(jmsDestination);
                        }
                    });
        }
    }


    protected List<JMSDestination> getQueues() {
        return new ArrayList<>(jmsManager.getDestinations().values());
    }

    protected List<JMSDestination> getQueuesAuthenticated() {
        return authUtils.runFunctionWithSecurityContext(this::getQueues,
                "jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
    }

    protected List<JMSDestination> getQueueNamesDLQ() {
        List<JMSDestination> lstAllQueues = getQueuesAuthenticated();
        if(lstAllQueues == null) {
            return new ArrayList<>();
        }
        return lstAllQueues.stream().filter(jmsDestination -> StringUtils.containsIgnoreCase(jmsDestination.getName(), "domibus")
                && StringUtils.containsIgnoreCase(jmsDestination.getName(), "DLQ")).collect(Collectors.toList());
    }

    protected long getQueueSize(final JMSDestination jmsDestination) {
        return domainTaskExecutor.submit(() -> {
            final long queueSize = authUtils.runFunctionWithSecurityContext(() -> jmsManager.getDestinationSize(jmsDestination),
                    "jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
            LOG.debug("getQueueSize for queue=[{}] returned count=[{}]", jmsDestination.getName(), queueSize);
            return queueSize;
        });
    }

}
