package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_METRICS_MONITOR_JMS_QUEUES;

/**
 * Scheduler class checking that the queues are properly initiated in cluster environment before adding them to the metrics.
 *
 * @author Thomas Dussart
 * @since 4.2.12
 */
public class JmsQueueCountSetScheduler {

    protected static final Logger LOG = LoggerFactory.getLogger(JmsQueueCountSetScheduler.class);

    public static final String JMS_QUEUES = "jmsQueues";

    public static final int DELAY = 20;

    public static final int PERIOD = 1000;

    private MetricRegistry metricRegistry;

    private JMSQueuesCountSet jmsQueuesCountSet;

    private DomibusPropertyProvider domibusPropertyProvider;

    private java.util.Timer timer;

    public JmsQueueCountSetScheduler(MetricRegistry metricRegistry, JMSQueuesCountSet jmsQueuesCountSet, DomibusPropertyProvider domibusPropertyProvider) {
        this.metricRegistry = metricRegistry;
        this.jmsQueuesCountSet = jmsQueuesCountSet;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    protected boolean registerQueues() {
        Boolean monitorJMSQueues = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES);
        if (BooleanUtils.isFalse(monitorJMSQueues)) {
            LOG.info("Metrics monitoring on queue is disabled.");
            return true;
        }
        if (isQueueMetricsRegistered()) {
            LOG.debug("Queue metrics with key prefix:[{}] already registered on server", JMS_QUEUES);
            return true;
        }
        if (jmsQueuesCountSet.isNodeReady()) {
            LOG.debug("Registering Queue metrics with key:[{}] on node server", JMS_QUEUES);
            metricRegistry.register(JMS_QUEUES, jmsQueuesCountSet);
            return true;
        }
        return false;
    }

    protected boolean isQueueMetricsRegistered() {
        return metricRegistry.getMetrics().keySet().stream().anyMatch(keyName -> keyName.startsWith(JMS_QUEUES));
    }

    public void initialize() {
        Boolean monitorJMSQueues = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES);
        if(BooleanUtils.isFalse(monitorJMSQueues)){
            LOG.info("Metrics monitoring on queue is disabled.");
            return;
        }
        LOG.info("Context initialized.");
        this.timer = new java.util.Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOG.info("Execute timer to register queues metrics gauge.");
                boolean registered = JmsQueueCountSetScheduler.this.registerQueues();
                if (registered) {
                    LOG.info("Queues metrics gauge registered. Canceling timer");
                    cancel();
                }
            }
        }, DELAY, PERIOD);
    }

}
