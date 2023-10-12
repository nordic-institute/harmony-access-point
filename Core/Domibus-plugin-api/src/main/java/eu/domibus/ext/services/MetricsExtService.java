package eu.domibus.ext.services;

import eu.domibus.ext.domain.metrics.MetricRegistryDTO;

/**
 * Metrics service
 */
public interface MetricsExtService {

    /**
     *
     * @return {@link com.codahale.metrics.MetricRegistry}
     */
    MetricRegistryDTO getMetricRegistry();

}
