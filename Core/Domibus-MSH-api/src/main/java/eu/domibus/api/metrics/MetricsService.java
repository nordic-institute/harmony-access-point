package eu.domibus.api.metrics;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface MetricsService {

    /**
     *
     * @return {@link com.codahale.metrics.MetricRegistry}
     */
    Object getMetricRegistry();

}
