package eu.domibus.core.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.databind.util.JSONPObject;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMetricsServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json");
        if (allowedOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        }
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setStatus(HttpServletResponse.SC_OK);

        try (OutputStream output = resp.getOutputStream()) {
            if (jsonpParamName != null && req.getParameter(jsonpParamName) != null) {
                getWriter(req).writeValue(output, new JSONPObject(req.getParameter(jsonpParamName), registry));
            } else {
                getWriter(req).writeValue(output, registry);
            }
        } catch (IOException e) {
            LOG.error("Error in write to HttpServletResponse output", e);
        }
    }


    protected void register(MetricRegistry metricRegistry, Map.Entry<String, Metric> entry) {
        try {
            metricRegistry.register(entry.getKey(), entry.getValue());
            LOG.debug("printing metric name=[{}] value=[{}]", entry.getKey(), entry.getValue());
        } catch (IllegalArgumentException e) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Error in printing metric name=[" + entry.getKey() + "] value=[" + entry.getValue() + "]", e);
        }
    }
}
