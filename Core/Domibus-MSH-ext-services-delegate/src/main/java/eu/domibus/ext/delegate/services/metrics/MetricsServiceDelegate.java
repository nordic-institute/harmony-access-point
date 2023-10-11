package eu.domibus.ext.delegate.services.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.metrics.MetricsService;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.ext.services.MetricsExtService;
import org.springframework.stereotype.Service;

/**
 * @author François GAUTIER
 * @since 5.0
 */
@Service
public class MetricsServiceDelegate implements MetricsExtService {

    private final MetricsService metricsService;

    public MetricsServiceDelegate(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetricRegistry getMetricRegistry() throws DomibusMonitoringExtException {
        return metricsService.getMetricRegistry();
    }

}
