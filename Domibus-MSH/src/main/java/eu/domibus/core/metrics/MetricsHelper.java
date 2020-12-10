package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.spring.SpringContextProvider;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @author Catalin Enache
 * @since 4.1
 */
@Component
public class MetricsHelper {

    private DomibusConfigurationService domibusConfigurationService;
    private AuthUtils authUtils;

    private MetricsHelper(DomibusConfigurationService domibusConfigurationService, AuthUtils authUtils) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.authUtils = authUtils;
    }

    public boolean showJMSCounts() {
        return (domibusConfigurationService.isSingleTenantAware() || (domibusConfigurationService.isMultiTenantAware() && authUtils.isSuperAdmin()));
    }

    public static MetricRegistry getMetricRegistry() {
        return SpringContextProvider.getApplicationContext().getBean("domibusMetricRegistry", MetricRegistry.class);
    }
}
