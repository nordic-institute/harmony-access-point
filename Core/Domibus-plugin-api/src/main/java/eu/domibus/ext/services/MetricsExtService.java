package eu.domibus.ext.services;

import com.codahale.metrics.MetricRegistry;

/**
 * Metrics service
 */
public interface MetricsExtService {

    /**
     *
     * @return {@link com.codahale.metrics.MetricRegistry}
     */
    MetricRegistry getMetricRegistry();

}
