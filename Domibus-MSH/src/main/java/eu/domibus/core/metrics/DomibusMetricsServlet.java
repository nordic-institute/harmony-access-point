package eu.domibus.core.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class DomibusMetricsServlet extends MetricsServlet {

    @Autowired
    MetricsHelper metricsHelper;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }


    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {
        boolean showJMSCount = metricsHelper.showJMSCounts();

        //create a copy of existing for metric registry
        MetricRegistry metricRegistry = new MetricRegistry();
        for (Map.Entry<String, Metric> entry : registry.getMetrics().entrySet()) {
            if (!showJMSCount && entry.getKey().startsWith(MetricsConfiguration.JMS_QUEUES)) {
                continue;
            }
            metricRegistry.register(entry.getKey(), entry.getValue());
        }

        resp.setContentType("application/json");
        if (allowedOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        }
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setStatus(HttpServletResponse.SC_OK);

        try (OutputStream output = resp.getOutputStream()) {
            if (jsonpParamName != null && req.getParameter(jsonpParamName) != null) {
                getWriter(req).writeValue(output, new JSONPObject(req.getParameter(jsonpParamName), metricRegistry));
            } else {
                getWriter(req).writeValue(output, metricRegistry);
            }
        }
    }
}
