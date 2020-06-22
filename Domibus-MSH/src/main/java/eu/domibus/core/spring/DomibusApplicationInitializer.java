package eu.domibus.core.spring;

import com.codahale.metrics.servlets.AdminServlet;
import com.google.common.collect.Sets;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.plugin.PluginException;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.metrics.HealthCheckServletContextListener;
import eu.domibus.core.metrics.MetricsServletContextListener;
import eu.domibus.core.property.DomibusConfigLocationProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.logging.LogbackLoggingConfigurator;
import eu.domibus.core.plugin.classloader.PluginClassLoader;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.DomibusPropertyConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DomibusApplicationInitializer implements WebApplicationInitializer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusApplicationInitializer.class);

    public static final String PLUGINS_LOCATION = "/plugins/lib";
    public static final String EXTENSIONS_LOCATION = "/extensions/lib";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String domibusConfigLocation = new DomibusConfigLocationProvider().getDomibusConfigLocation(servletContext);
        LOG.debug("Configured property [{}] with value [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);

        BouncyCastleInitializer bouncyCastleInitializer = new BouncyCastleInitializer();
        bouncyCastleInitializer.registerBouncyCastle();
        bouncyCastleInitializer.checkStrengthJurisdictionPolicyLevel();

        configureLogging(domibusConfigLocation);

        PluginClassLoader pluginClassLoader = createPluginClassLoader(domibusConfigLocation);
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(DomibusRootConfiguration.class);

        try {
            configurePropertySources(rootContext, domibusConfigLocation);
        } catch (IOException e) {
            throw new ServletException("Error configuring property sources", e);
        }

        servletContext.addListener(new DomibusContextLoaderListener(rootContext, pluginClassLoader));
        servletContext.addListener(new RequestContextListener());

        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(DomibusWebConfiguration.class);
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        Set<SessionTrackingMode> sessionTrackingModes = new HashSet<>();
        sessionTrackingModes.add(SessionTrackingMode.COOKIE);
        servletContext.setSessionTrackingModes(sessionTrackingModes);

        FilterRegistration.Dynamic springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
        springSecurityFilterChain.addMappingForUrlPatterns(null, false, "/*");

        ServletRegistration.Dynamic cxfServlet = servletContext.addServlet("CXF", CXFServlet.class);
        cxfServlet.setLoadOnStartup(1);
        cxfServlet.addMapping("/services/*");

        configureMetrics(servletContext);
    }

    protected void configureMetrics(ServletContext servletContext) {
        servletContext.addListener(new MetricsServletContextListener());
        servletContext.addListener(new HealthCheckServletContextListener());

        ServletRegistration.Dynamic servlet = servletContext.addServlet("metrics", AdminServlet.class);
        servlet.addMapping("/metrics/*");
    }

    protected PluginClassLoader createPluginClassLoader(String domibusConfigLocation) {
        String pluginsLocation = domibusConfigLocation + PLUGINS_LOCATION;
        String extensionsLocation = domibusConfigLocation + EXTENSIONS_LOCATION;

        LOG.info("Using plugins location [{}]", pluginsLocation);

        Set<File> pluginsDirectories = Sets.newHashSet(new File(pluginsLocation));
        if (StringUtils.isNotEmpty(extensionsLocation)) {
            LOG.info("Using extension location [{}]", extensionsLocation);
            pluginsDirectories.add(new File(extensionsLocation));
        }

        PluginClassLoader pluginClassLoader = null;
        try {
            pluginClassLoader = new PluginClassLoader(pluginsDirectories, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "Malformed URL Exception", e);
        }
        return pluginClassLoader;
    }

    protected void configureLogging(String domibusConfigLocation) {
        try {
            //we need to initialize the logging before Spring is being initialized
            LogbackLoggingConfigurator logbackLoggingConfigurator = new LogbackLoggingConfigurator(domibusConfigLocation);
            logbackLoggingConfigurator.configureLogging();
        } catch (RuntimeException e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occurred while configuring logging", e);
        }
    }

    protected void configurePropertySources(AnnotationConfigWebApplicationContext rootContext, String domibusConfigLocation) throws IOException {
        ConfigurableEnvironment configurableEnvironment = rootContext.getEnvironment();
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        MapPropertySource domibusConfigLocationSource = createDomibusConfigLocationSource(domibusConfigLocation);
        propertySources.addFirst(domibusConfigLocationSource);

        DomibusPropertiesPropertySource domibusPropertiesPropertySource = createDomibusPropertiesPropertySource(domibusConfigLocation);
        propertySources.addLast(domibusPropertiesPropertySource);
    }

    public DomibusPropertiesPropertySource createDomibusPropertiesPropertySource(String domibusConfigLocation) throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new DomibusPropertyConfiguration().domibusProperties(domibusConfigLocation);
        propertiesFactoryBean.setSingleton(false);
        Properties properties = propertiesFactoryBean.getObject();
        return new DomibusPropertiesPropertySource(DomibusPropertiesPropertySource.NAME, properties);
    }

    protected MapPropertySource createDomibusConfigLocationSource(String domibusConfigLocation) {
        Map domibusConfigLocationMap = new HashMap();
        domibusConfigLocationMap.put(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
        return new MapPropertySource("domibusConfigLocationSource", domibusConfigLocationMap);
    }


}
