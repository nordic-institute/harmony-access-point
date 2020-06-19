package eu.domibus.core.logging;

import ch.qos.logback.classic.LoggerContext;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author François Gautier
 * @since 4.2
 */
public class LogbackShutdownHook implements ServletContextListener {

    static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LogbackShutdownHook.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Shutting down ch.qos.logback.classic.LoggerContext");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
