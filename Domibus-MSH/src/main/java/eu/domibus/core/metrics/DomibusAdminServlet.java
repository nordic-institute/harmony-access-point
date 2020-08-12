package eu.domibus.core.metrics;

import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.MetricsServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public class DomibusAdminServlet extends AdminServlet {

    private transient MetricsServlet domibusMetricsServlet;
    private transient String metricsUri;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.domibusMetricsServlet = new DomibusMetricsServlet();
        domibusMetricsServlet.init(config);

        this.metricsUri = config.getInitParameter(METRICS_URI_PARAM_KEY) != null ? config.getInitParameter(METRICS_URI_PARAM_KEY) : DEFAULT_METRICS_URI;
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri == null || uri.equals("/")) {
            super.service(req, resp);
        } else if (uri.startsWith(metricsUri)) {
            domibusMetricsServlet.service(req, resp);
        } else {
            super.service(req, resp);
        }
    }
}
