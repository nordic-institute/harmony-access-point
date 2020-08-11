package eu.domibus.core.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        super.contextInitialized(sce);
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        final MetricsHelper metricsHelper = (MetricsHelper) webApplicationContext.getBean("metricsHelper");
        return metricsHelper.getHealthCheckRegistry();
    }
}
