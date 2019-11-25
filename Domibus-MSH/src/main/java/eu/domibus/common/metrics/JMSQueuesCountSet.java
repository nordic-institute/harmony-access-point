package eu.domibus.common.metrics;

import com.codahale.metrics.*;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * seconds
     */
    private long refreshPeriod;

    private boolean showDLQOnly;

    /**
     * @param jmsManager
     * @param authUtils
     * @param refreshPeriod how long (in seconds) the value will be cached
     * @param showDLQOnly
     */
    public JMSQueuesCountSet(JMSManager jmsManager, AuthUtils authUtils, long refreshPeriod, boolean showDLQOnly) {
        this.jmsManager = jmsManager;
        this.authUtils = authUtils;
        this.refreshPeriod = refreshPeriod;
        this.showDLQOnly = showDLQOnly;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        List<String> queueNames = showDLQOnly ? getQueueNamesDLQ() : getQueueNames();
        LOG.debug("Using queues [{}] for metrics with refreshPeriod=[{}]", queueNames, refreshPeriod);

        for (String queueName : queueNames) {
            addQueueCountToMetrics(gauges, queueName, refreshPeriod);
        }
        return gauges;
    }

    private void addQueueCountToMetrics(Map<String, Metric> gauges, final String queueName, final long refreshPeriod) {
        if (refreshPeriod == 0) {
            //no cached metrics
            gauges.put(MetricRegistry.name(queueName),
                    (Gauge<Long>) () -> getQueueSize(queueName));
        } else {
            gauges.put(MetricRegistry.name(queueName),
                    new CachedGauge<Long>(refreshPeriod, TimeUnit.SECONDS) {
                        @Override
                        protected Long loadValue() {
                            return getQueueSize(queueName);
                        }
                    });
        }
    }

    private void assureSecurityRights() {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
        }
    }

    private List<String> getQueueNames() {
        assureSecurityRights();
        return jmsManager.getDestinations().values().stream().map(JMSDestination::getName).collect(Collectors.toList());
    }

    private List<String> getQueueNamesDLQ() {
        return getQueueNames().stream().filter(s -> StringUtils.containsIgnoreCase(s, "domibus")
                && StringUtils.containsIgnoreCase(s, "DLQ")).collect(Collectors.toList());
    }

    private long getQueueSize(final String queueName) {
        assureSecurityRights();

        // time consuming mostly on cluster configuration
        //TODO EDELIVERY-5557
        final long queueSize = jmsManager.getDestinationSize(queueName);
        LOG.debug("getQueueSize for queue=[{}] returned count=[{}]", queueName, queueSize);
        return queueSize;
    }

}
