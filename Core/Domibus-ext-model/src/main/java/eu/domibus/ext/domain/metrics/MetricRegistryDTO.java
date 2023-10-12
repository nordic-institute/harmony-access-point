package eu.domibus.ext.domain.metrics;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Soumya Chandran
 * @since 5.1.2
 */
public class MetricRegistryDTO {
    private MetricRegistry metricRegistry;

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("metricRegistry", metricRegistry)
                .toString();
    }
}
