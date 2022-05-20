package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MetricsConfigurationTest {

    @Tested
    MetricsConfiguration metricsConfiguration;


    @Test
    public void metricRegistry(final @Mocked DomibusPropertyProvider domibusPropertyProvider,
                               final @Mocked JMSManager jmsManager, final @Mocked AuthUtils authUtils,
                               final @Mocked DomainTaskExecutor domainTaskExecutor,
                               final @Mocked MetricRegistry metricRegistry) {

        new Expectations(metricsConfiguration) {{
            metricsConfiguration.createMetricRegistry(domibusPropertyProvider, jmsManager, authUtils, domainTaskExecutor);
            result = metricRegistry;
        }};

        //tested method
        metricsConfiguration.metricRegistry(domibusPropertyProvider, jmsManager, authUtils, domainTaskExecutor);

        new FullVerifications(metricsConfiguration) {{
            metricsConfiguration.addMetricsToLogs(domibusPropertyProvider, metricRegistry);
        }};
    }

    @Test
    public void createMetricRegistry(final @Mocked MetricRegistry metricRegistry,
                                     final @Mocked DomibusPropertyProvider domibusPropertyProvider,
                                     final @Mocked JMSManager jmsManager,
                                     final @Mocked AuthUtils authUtils,
                                     final @Mocked DomainTaskExecutor domainTaskExecutor) {

        new Expectations() {{
            new MetricRegistry();

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_MEMORY);
            result = true;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_GC);
            result = true;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS);
            result = true;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE);
            result = true;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES);
            result = true;

            domibusPropertyProvider.getProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_REFRESH_PERIOD);
            result = 10;

            domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_JMS_QUEUES_SHOW_DLQ_ONLY);
            result = false;

        }};

        //tested method
        metricsConfiguration.createMetricRegistry(domibusPropertyProvider, jmsManager, authUtils, domainTaskExecutor);

        new VerificationsInOrder() {{
            String name;
            metricRegistry.register(name = withCapture(), withAny(new MemoryUsageGaugeSet()));
            Assert.assertEquals("memory", name);

            metricRegistry.register(name = withCapture(), withAny(new GarbageCollectorMetricSet()));
            Assert.assertEquals("gc", name);

            metricRegistry.register(name = withCapture(), withAny(new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS)));
            Assert.assertEquals("threads", name);

            metricRegistry.register(name = withCapture(), withAny(new JMSQueuesCountSet(jmsManager, authUtils, domainTaskExecutor,
                    10L, false)));
            Assert.assertEquals(MetricsConfiguration.JMS_QUEUES, name);

        }};
    }
}