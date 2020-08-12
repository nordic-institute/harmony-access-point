package eu.domibus.core.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    private ServletContext servletContext;

    @Autowired
    private MetricRegistry metricRegistry;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        super.contextInitialized(sce);
    }


    @Override
    protected MetricRegistry getMetricRegistry() {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, servletContext);
        return metricRegistry;
    }
}
