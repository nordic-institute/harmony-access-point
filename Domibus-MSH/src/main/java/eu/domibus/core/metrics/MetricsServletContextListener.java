package eu.domibus.core.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Override
    protected MetricRegistry getMetricRegistry() {
        return MetricsHelper.getMetricRegistry();
    }
}
