package eu.domibus.core.spring;

import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class ShutdownUtils {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ShutdownUtils.class);

    public static void shutdownDomibus(ApplicationContext applicationContext, boolean forceExit) {
        try {
            if (applicationContext instanceof ConfigurableApplicationContext) {
                ((ConfigurableApplicationContext)applicationContext).close();
            }
        } catch (Exception ex) {
            LOG.error("Could not close application context", ex);
        }
        shutdownDomibus(forceExit);
    }

    public static void shutdownDomibus(boolean forceExit) {
        try {
            LOG.warn(WarningUtil.warnOutput("Domibus is stopping."));
            shutdownLogger();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (forceExit) {
            System.exit(1);
        }
    }

    public static void shutdownLogger() {
        LOG.info("Stop ch.qos.logback.classic.LoggerContext");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
