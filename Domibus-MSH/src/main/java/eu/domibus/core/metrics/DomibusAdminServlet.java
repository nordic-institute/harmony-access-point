package eu.domibus.core.metrics;

import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * SuppressWarnings:
 * Following the pattern of {@link AdminServlet}, the sonar issue S2226 should be ignore here.
 * @see <a href="https://jira.sonarsource.com/browse/RSPEC-2226">RSPEC-2226 Servlets should not have mutable instance fields</a>
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class DomibusAdminServlet extends AdminServlet {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusAdminServlet.class);

    @SuppressWarnings("squid:S2226")
    private transient MetricsServlet domibusMetricsServlet;
    @SuppressWarnings("squid:S2226")
    private transient String metricsUri;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.domibusMetricsServlet = new DomibusMetricsServlet();
        this.domibusMetricsServlet.init(config);

        this.metricsUri = config.getInitParameter(METRICS_URI_PARAM_KEY) != null ? config.getInitParameter(METRICS_URI_PARAM_KEY) : DEFAULT_METRICS_URI;
        LOG.debug("metricsUri=[{}]", this.metricsUri);
    }


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getPathInfo();
        if (uri != null && uri.startsWith(metricsUri)) {
            LOG.debug("calling domibusMetricsServlet service");
            domibusMetricsServlet.service(req, resp);
        } else {
            LOG.debug("calling super service");
            super.service(req, resp);
        }
    }
}
