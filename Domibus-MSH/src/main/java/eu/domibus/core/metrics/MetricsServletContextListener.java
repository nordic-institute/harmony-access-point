package eu.domibus.core.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletContextEvent;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Autowired
    MetricsHelper metricsHelper;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,  sce.getServletContext());
    }


    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricsHelper.getMetricRegistry();
    }
}
