package eu.domibus.core.spring;

import eu.domibus.core.plugin.classloader.PluginClassLoader;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
        // Set after Spring/CXF context refresh completes so CXF can load spring-beans.xsd during startup,
        // but before the servlet container accepts requests — CVE-2026-49875 workaround.
        //TODO: EDELIVERY-16973: remove after the Cxf library is upgraded to a JAXP hardened one
        System.setProperty("javax.xml.accessExternalDTD", "");
        System.setProperty("javax.xml.accessExternalSchema", "");
        LOG.info("JAXP external DTD and schema access restricted (CVE-2026-49875 workaround)");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOG.info("DomibusContextLoaderListener contextDestroyed");

        super.contextDestroyed(servletContextEvent);
        shutdownPluginClassLoader();
        ShutdownUtils.shutdownLogger();
    }

    protected void shutdownPluginClassLoader() {
        if (pluginClassLoader == null) {
            return;
        }
        try {
            LOG.info("Closing PluginClassLoader");
            pluginClassLoader.close();
        } catch (IOException e) {
            LOG.warn("Error closing PluginClassLoader", e);
        }
    }
}
