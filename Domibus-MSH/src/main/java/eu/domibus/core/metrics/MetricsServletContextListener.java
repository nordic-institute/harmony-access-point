package eu.domibus.core.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Autowired
    private MetricRegistry metricRegistry;

    @Override
    protected MetricRegistry getMetricRegistry() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        return metricRegistry;
    }
}
