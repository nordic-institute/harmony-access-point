package eu.domibus.core.spring;

import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.plugin.classloader.PluginClassLoader;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.constructs.web.ShutdownListener;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextLoaderListener.class);

    protected PluginClassLoader pluginClassLoader;

    public DomibusContextLoaderListener(WebApplicationContext context, PluginClassLoader pluginClassLoader) {
        super(context);
        this.pluginClassLoader = pluginClassLoader;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        LOG.info("Shutting down net.sf.ehcache");
        new ShutdownListener().contextDestroyed(servletContextEvent);

        if (pluginClassLoader != null) {
            try {
                LOG.info("Closing PluginClassLoader");
                pluginClassLoader.close();
            } catch (IOException e) {
                LOG.warn("Error closing PluginClassLoader", e);
            }
        }
        LOG.info("Stop ch.qos.logback.classic.LoggerContext");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }
}
