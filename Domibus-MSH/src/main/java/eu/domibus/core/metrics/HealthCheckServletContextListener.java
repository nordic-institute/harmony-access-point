package eu.domibus.core.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

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
