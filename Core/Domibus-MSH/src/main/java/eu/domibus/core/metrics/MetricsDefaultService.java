package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.metrics.MetricsService;
import org.springframework.stereotype.Service;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class MetricsDefaultService implements MetricsService {

    private final MetricRegistry metricRegistry;

    public MetricsDefaultService(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

}
