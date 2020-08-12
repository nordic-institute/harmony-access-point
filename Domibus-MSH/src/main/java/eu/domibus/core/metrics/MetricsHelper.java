package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @author Catalin Enache
 * @since 4.1
 */
@Component("metricsHelper")
public class MetricsHelper {

    private MetricRegistry metricRegistry;

    private HealthCheckRegistry healthCheckRegistry;

    private DomibusConfigurationService domibusConfigurationService;

    private AuthUtils authUtils;

    private MetricsHelper(MetricRegistry metricRegistry,
                          HealthCheckRegistry healthCheckRegistry,
                          DomibusConfigurationService domibusConfigurationService,
                          AuthUtils authUtils) {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.domibusConfigurationService = domibusConfigurationService;
        this.authUtils = authUtils;
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return this.healthCheckRegistry;
    }

    public boolean showJMSCounts() {
        return (domibusConfigurationService.isSingleTenant() || (domibusConfigurationService.isMultiTenantAware() && authUtils.isSuperAdmin()));
    }
}
