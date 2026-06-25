package eu.domibus.core.spring;

import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Ionut Breaz
 * @since 5.1.5
 */

public class ShutdownUtils {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ShutdownUtils.class);

    public static void shutdownDomibus(ApplicationContext applicationContext) {
        try {
            if (applicationContext instanceof ConfigurableApplicationContext) {
                LOG.warn("Stopping the applicationContext");
                ((ConfigurableApplicationContext)applicationContext).close();
            }
        } catch (Exception ex) {
            LOG.error("Could not close application context", ex);
        }
        shutdownLogger();
        // logger is stopped, so we use the old way
        System.err.println("Calling System.exit(1)");
        System.exit(1);
    }

    public static void shutdownLogger() {
        try {
            LOG.warn(WarningUtil.warnOutput("Domibus is stopping."));
            LOG.info("Stop ch.qos.logback.classic.LoggerContext");
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            Thread shutdownHook = (Thread) loggerContext.getObject("SHUTDOWN_HOOK");
            if (shutdownHook != null) {
                LOG.info("Explicitly running the logback shutdown hook [{}]", shutdownHook.getClass().getName());
                // Use run() instead of start() to execute synchronously in the current thread
                shutdownHook.run();
            } else {
                // Safe to call stop() directly as a fallback; it performs the same cleanup as the shutdown hook
                loggerContext.stop();
            }

        } catch (Exception ex) {
            // logger is stopping, so we cannot use it to log the exception
            ex.printStackTrace();
        }
    }
}
