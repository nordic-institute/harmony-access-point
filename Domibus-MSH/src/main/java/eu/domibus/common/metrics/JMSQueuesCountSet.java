package eu.domibus.common.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

    public JMSQueuesCountSet(JMSManager jmsManager, AuthUtils authUtils) {
        this.jmsManager = jmsManager;
        this.authUtils = authUtils;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        if(!authUtils.isUnsecureLoginAllowed()) {
            authUtils.setAuthenticationToSecurityContext("jms_metrics_user", "jms_metrics_password", AuthRole.ROLE_AP_ADMIN);
        }

        Map<String, JMSDestination> queues = jmsManager.getDestinations();
        for (Map.Entry<String, JMSDestination> entry: queues.entrySet()) {
            final JMSDestination jmsDestination = entry.getValue();
            LOG.debug("Getting the count for [{}]", jmsDestination);
            final String queueName = jmsDestination.getName();
            final long queueNbOfMessages = jmsDestination.getNumberOfMessages();
            gauges.put(queueName, (Gauge<Long>) () -> queueNbOfMessages);
        }
        return gauges;
    }
}
