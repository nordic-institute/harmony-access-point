package eu.domibus.core.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DomibusMetricsServlet extends MetricsServlet {

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(req.getServletContext());

        final MetricsHelper metricsHelper = (MetricsHelper) webApplicationContext.getBean("metricsHelper");
        MetricRegistry registry = metricsHelper.getMetricRegistry();

        super.registry = registry;

        super.doGet(req, resp);
    }
}
