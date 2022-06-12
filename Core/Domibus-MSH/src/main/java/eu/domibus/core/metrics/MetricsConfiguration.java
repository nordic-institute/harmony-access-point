package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@Configuration
public class MetricsConfiguration {

    protected static final Logger LOG = LoggerFactory.getLogger(MetricsConfiguration.class);

    protected static final Marker STATISTIC_MARKER = MarkerFactory.getMarker("STATISTIC");
    public static final String JMS_QUEUES = "jmsQueues";

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @Bean
    public MetricRegistry metricRegistry(DomibusPropertyProvider domibusPropertyProvider,
                                         JMSManager jmsManager, AuthUtils authUtils, DomainTaskExecutor domainTaskExecutor) {

        MetricRegistry metricRegistry = createMetricRegistry(domibusPropertyProvider, jmsManager, authUtils, domainTaskExecutor);
        addMetricsToLogs(domibusPropertyProvider, metricRegistry);
        return metricRegistry;
    }


    protected MetricRegistry createMetricRegistry(DomibusPropertyProvider domibusPropertyProvider, JMSManager jmsManager,
                                                  AuthUtils authUtils, DomainTaskExecutor domainTaskExecutor) {
        MetricRegistry metricRegistry = new MetricRegistry();
        boolean monitorMemory = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_MEMORY);

        boolean monitorGc = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_GC);

        boolean monitorCachedThread = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS);

        if (monitorMemory) {
            metricRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        if (monitorGc) {
            metricRegistry.register("gc", new GarbageCollectorMetricSet());
        }
        if (monitorCachedThread) {
            metricRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
        }

        Boolean jmxReporterEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE);
        if (jmxReporterEnabled) {
            LOG.info("Jmx metrics reporter enabled");
            JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
            jmxReporter.start();
        }

        Boolean monitorJMSQueues = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES);
        if (monitorJMSQueues) {
            long refreshPeriod = NumberUtils.toLong(domibusPropertyProvider.getProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_REFRESH_PERIOD), 10);
            boolean showDLQOnly = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_SHOW_DLQ_ONLY);
            metricRegistry.register(JMS_QUEUES, new JMSQueuesCountSet(jmsManager, authUtils, domainTaskExecutor,
                    refreshPeriod, showDLQOnly));
        }

        return metricRegistry;
    }

    protected void addMetricsToLogs(DomibusPropertyProvider domibusPropertyProvider, MetricRegistry metricRegistry) {
        Boolean sl4jReporterEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_ENABLE);
        if (sl4jReporterEnabled) {
            Integer periodProperty = domibusPropertyProvider.getIntegerProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER);
            String timeUnitProperty = domibusPropertyProvider.getProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT);
            TimeUnit timeUnit = TimeUnit.MINUTES;
            try {

                TimeUnit configuredTimeUnit = TimeUnit.valueOf(timeUnitProperty);
                switch (configuredTimeUnit) {
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                        timeUnit = configuredTimeUnit;
                        break;
                    default:
                        LOG.warn("Unsupported time unit property:[{}],setting default to MINUTE", timeUnitProperty);
                }
            } catch (IllegalArgumentException e) {
                LOG.warn("Invalid time unit property:[{}],setting default to MINUTE", timeUnitProperty, e);
            }
            LOG.info("Sl4j metrics reporter enabled with reporting time unit:[{}] and period:[{}]", timeUnit, periodProperty);
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(LoggerFactory.getLogger("eu.domibus.statistic"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .markWith(STATISTIC_MARKER)
                    .build();
            reporter.start(periodProperty, timeUnit);
        }
    }

}
