package eu.domibus.core.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    private ServletContext servletContext;

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        super.contextInitialized(sce);

    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return healthCheckRegistry;
    }
}
