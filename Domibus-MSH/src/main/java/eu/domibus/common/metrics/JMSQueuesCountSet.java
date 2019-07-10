package eu.domibus.common.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements {@code MetricSet} for JMSQueues counts
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
@Component
public class JMSQueuesCountSet implements MetricSet {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSQueuesCountSet.class);

    private JMSManager jmsManager;

    public JMSQueuesCountSet(JMSManager jmsManager) {
        this.jmsManager = jmsManager;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();

        Map<String, JMSDestination> queues = jmsManager.getDestinations();
        for (Map.Entry<String, JMSDestination> entry: queues.entrySet()) {
            final JMSDestination jmsDestination = entry.getValue();
            LOG.debug("Getting the count for {}", jmsDestination);
            final String queueName = jmsDestination.getName();
            final long queueNbOfMessages = jmsDestination.getNumberOfMessages();
            gauges.put(queueName, (Gauge<Long>) () -> queueNbOfMessages);
        }
        return gauges;
    }
}
