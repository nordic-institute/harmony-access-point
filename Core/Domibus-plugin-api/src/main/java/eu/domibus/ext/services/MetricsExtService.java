package eu.domibus.ext.services;

/**
 * Metrics service
 */
public interface MetricsExtService {

    /**
     *
     * @return {@link com.codahale.metrics.MetricRegistry}
     */
    Object getMetricRegistry();

}
