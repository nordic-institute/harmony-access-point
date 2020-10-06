package eu.domibus.core.metrics;

import com.codahale.metrics.*;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    /**
     * @param jmsManager
     * @param authUtils
     * @param refreshPeriod how long (in seconds) the value will be cached
     * @param showDLQOnly
     */
    public JMSQueuesCountSet(JMSManager jmsManager, AuthUtils authUtils, DomainTaskExecutor domainTaskExecutor, long refreshPeriod, boolean showDLQOnly) {
        this.jmsManager = jmsManager;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.refreshPeriod = refreshPeriod;
        this.showDLQOnly = showDLQOnly;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        List<JMSDestination> jmsDestinations = showDLQOnly ? getQueueNamesDLQ() : getQueuesAuthenticated();
        LOG.debug("Using queues [{}] for metrics with refreshPeriod=[{}]", jmsDestinations, refreshPeriod);

        for (JMSDestination jmsDestination : jmsDestinations) {
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
        return authUtils.wrapApplicationSecurityContextToFunction(this::getQueues,
                "jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
    }

    protected List<JMSDestination> getQueueNamesDLQ() {
        List<JMSDestination> lstAllQueues = getQueuesAuthenticated();
        return lstAllQueues.stream().filter(jmsDestination -> StringUtils.containsIgnoreCase(jmsDestination.getName(), "domibus")
                && StringUtils.containsIgnoreCase(jmsDestination.getName(), "DLQ")).collect(Collectors.toList());
    }

    protected long getQueueSize(final JMSDestination jmsDestination) {
        return domainTaskExecutor.submit(() -> {
            final long queueSize = authUtils.wrapApplicationSecurityContextToFunction(()->jmsManager.getDestinationSize(jmsDestination),
                    "jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
            LOG.debug("getQueueSize for queue=[{}] returned count=[{}]", jmsDestination.getName(), queueSize);
            return queueSize;
        });
    }

}
