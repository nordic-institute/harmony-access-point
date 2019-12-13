package eu.domibus.common.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import eu.domibus.api.metrics.MetricsHelper;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return MetricsHelper.getHealthCheckRegistry();
    }
}
