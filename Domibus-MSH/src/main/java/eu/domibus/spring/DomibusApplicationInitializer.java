package eu.domibus.spring;

import com.codahale.metrics.servlets.AdminServlet;
import com.google.common.collect.Sets;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.plugin.PluginException;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.common.metrics.HealthCheckServletContextListener;
import eu.domibus.common.metrics.MetricsServletContextListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.LogbackLoggingConfigurator;
import eu.domibus.plugin.classloader.PluginClassLoader;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextLoaderListener.class);

    private static final String PLUGINS_LOCATION = "/plugins/lib";
    private static final String EXTENSIONS_LOCATION = "/extensions/lib";

    protected PluginClassLoader pluginClassLoader = null;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        String domibusConfigLocation = new DomibusConfigLocationProvider().getDomibusConfigLocation(servletContext);
        LOG.debug("Using [{}] with value [{}]", DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);

        try {
            //at this stage Spring is not yet initialized so we need to manually get the domibus.config.location
            LogbackLoggingConfigurator logbackLoggingConfigurator = new LogbackLoggingConfigurator(domibusConfigLocation);
            logbackLoggingConfigurator.configureLogging();
        } catch (Exception e) {
            //logging configuration problems should not prevent the application to startup
            LOG.warn("Error occurred while configuring logging", e);
        }


        Properties properties = new Properties();
        if (StringUtils.isNotEmpty(domibusConfigLocation)) {
            properties.setProperty(DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
        }
        if (servletContext.getAttribute(DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION) == null) {
            LOG.debug("Setting servlet context attribute [{}}] to [{}]", DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
            servletContext.setAttribute(DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
        }

        String pluginsLocation = domibusConfigLocation + PLUGINS_LOCATION;
        String extensionsLocation = domibusConfigLocation + EXTENSIONS_LOCATION;

        LOG.info("Using plugins location [{}]", pluginsLocation);

        Set<File> pluginsDirectories = Sets.newHashSet(new File(pluginsLocation));
        if (StringUtils.isNotEmpty(extensionsLocation)) {
            LOG.info("Using extension location [{}]", extensionsLocation);
            pluginsDirectories.add(new File(extensionsLocation));
        }

        try {
            pluginClassLoader = new PluginClassLoader(pluginsDirectories, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "Malformed URL Exception", e);
        }
        Thread.currentThread().setContextClassLoader(pluginClassLoader);


//        XmlWebApplicationContext rootContext = new XmlWebApplicationContext();
//        rootContext.setConfigLocation(
//                "classpath:META-INF/cxf/cxf.xml," +
//                        "classpath:META-INF/cxf/cxf-extension-jaxws.xml," +
//                        "classpath:META-INF/cxf/cxf-servlet.xml," +
//                        "classpath*:META-INF/resources/WEB-INF/spring-context.xml");
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(DomibusConfiguration.class);

        try {
            configurePropertySources(rootContext, domibusConfigLocation);
        } catch (IOException e) {
            throw new ServletException("Error configuring property sources", e);
        }

//        servletContext.addListener(new DomibusLoggingConfiguratorListener());
        servletContext.addListener(new DomibusContextLoaderListener(rootContext, pluginClassLoader));
        servletContext.addListener(new RequestContextListener());
        servletContext.addListener(new MetricsServletContextListener());
        servletContext.addListener(new HealthCheckServletContextListener());


//        XmlWebApplicationContext dispatcherContext = new XmlWebApplicationContext();
//        dispatcherContext.setConfigLocation("classpath:META-INF/resources/WEB-INF/mvc-dispatcher-servlet.xml");
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(DomibusWebConfiguration.class);
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");

        Set<SessionTrackingMode> sessionTrackingModes = new HashSet<>();
        sessionTrackingModes.add(SessionTrackingMode.COOKIE);
        servletContext.setSessionTrackingModes(sessionTrackingModes);

        DelegatingFilterProxy delegateFilterProxy = new DelegatingFilterProxy();
        FilterRegistration.Dynamic springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain", delegateFilterProxy);
        springSecurityFilterChain.addMappingForUrlPatterns(null, false, "/*");


        ServletRegistration.Dynamic cxfServlet = servletContext.addServlet("CXF", CXFServlet.class);
        cxfServlet.setLoadOnStartup(1);
        cxfServlet.addMapping("/services/*");

        ServletRegistration.Dynamic servlet = servletContext.addServlet("metrics", AdminServlet.class);
        servlet.addMapping("/metrics/*");
    }

    protected void configurePropertySources(AnnotationConfigWebApplicationContext rootContext, String domibusConfigLocation) throws IOException {
        ConfigurableEnvironment environment = rootContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        Map domibusConfigLocationMap = new HashMap();
        domibusConfigLocationMap.put(DomibusPropertyMetadataManager.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
        propertySources.addFirst(new MapPropertySource("domibusConfigLocationSource", domibusConfigLocationMap));

        PropertiesFactoryBean propertiesFactoryBean = new DomibusPropertyConfiguration().domibusProperties(domibusConfigLocation);
        propertiesFactoryBean.setSingleton(false);
        Properties properties = propertiesFactoryBean.getObject();
        DomibusPropertiesPropertySource propertySource = new DomibusPropertiesPropertySource(DomibusPropertiesPropertySource.NAME, properties);
        propertySources.addLast(propertySource);
    }


}
