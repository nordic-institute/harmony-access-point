package eu.domibus.api.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface MetricsService {

    /**
     *
     * @return {@link com.codahale.metrics.MetricRegistry}
     */
    MetricRegistry getMetricRegistry();

}
