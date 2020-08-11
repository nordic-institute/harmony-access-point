package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@Component("metricsHelper")
public class MetricsHelper {

    private MetricRegistry metricRegistry;

    private HealthCheckRegistry healthCheckRegistry;

    private MetricRegistry metricRegistryMTAdmin;

    private DomibusConfigurationService domibusConfigurationService;

    private AuthUtils authUtils;

    private MetricsHelper(MetricRegistry metricRegistry,
                          HealthCheckRegistry healthCheckRegistry,
                          MetricRegistry metricRegistryMTAdmin,
                          DomibusConfigurationService domibusConfigurationService,
                          AuthUtils authUtils) {
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
        this.metricRegistryMTAdmin = metricRegistryMTAdmin;
        this.domibusConfigurationService = domibusConfigurationService;
        this.authUtils = authUtils;
    }

    public MetricRegistry getMetricRegistry() {

        if (domibusConfigurationService.isSingleTenant() || (domibusConfigurationService.isMultiTenantAware() && authUtils.isSuperAdmin())) {
            return metricRegistry;
        }
        return  metricRegistryMTAdmin;
    }



    public HealthCheckRegistry getHealthCheckRegistry() {
        return this.healthCheckRegistry;
    }

}
