package eu.domibus.core.metrics;

import com.codahale.metrics.servlets.AdminServlet;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletConfig;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@RunWith(JMockit.class)
public class DomibusAdminServletTest {

    @Tested
    DomibusAdminServlet domibusAdminServlet;

    @Test
    public void test_init(final @Injectable ServletConfig config,
                          final @Mocked DomibusMetricsServlet domibusMetricsServlet,
                          final @Mocked AdminServlet adminServlet) throws Exception {

        new Expectations() {{
            new DomibusMetricsServlet();
            result = domibusMetricsServlet;

            config.getInitParameter(DomibusAdminServlet.METRICS_URI_PARAM_KEY);
            result = "/metrics";

        }};

        //tested method
        domibusAdminServlet.init(config);

        new FullVerifications() {{
            adminServlet.init(config);
            domibusMetricsServlet.init(config);
        }};
    }


}