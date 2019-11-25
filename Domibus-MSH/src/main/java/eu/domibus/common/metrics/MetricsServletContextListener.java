package eu.domibus.common.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    protected static final Logger LOG = LoggerFactory.getLogger(MetricsServletContextListener.class);

    @Override
    protected MetricRegistry getMetricRegistry() {
        LOG.debug("returning MetricRegistry...");
        return MetricsHelper.getMetricRegistry();
    }
}
