package eu.domibus.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MetricsHelper {

    private static MetricRegistry metricRegistry;

    private static HealthCheckRegistry healthCheckRegistry;

    private MetricsHelper() {
    }

    public static MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public static void setMetricRegistry(MetricRegistry metricRegistry) {
        MetricsHelper.metricRegistry = metricRegistry;
    }

    public static HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    public static void setHealthCheckRegistry(HealthCheckRegistry healthCheckRegistry) {
        MetricsHelper.healthCheckRegistry = healthCheckRegistry;
    }
}
