package eu.domibus.logging;

import eu.domibus.spring.DomibusConfigLocationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class DomibusLoggingConfiguratorListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusLoggingConfiguratorListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        String domibusConfigLocation = new DomibusConfigLocationProvider().getDomibusConfigLocation(servletContext);

        try {
            //at this stage Spring is not yet initialized so we need to manually get the domibus.config.location
            LogbackLoggingConfigurator logbackLoggingConfigurator = new LogbackLoggingConfigurator(domibusConfigLocation);
            logbackLoggingConfigurator.configureLogging();
        } catch (Exception e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occurred while configuring logging", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //nothing to clean
    }
}
