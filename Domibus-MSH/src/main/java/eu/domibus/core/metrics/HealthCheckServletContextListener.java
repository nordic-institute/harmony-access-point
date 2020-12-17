package eu.domibus.core.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return healthCheckRegistry;
    }
}
