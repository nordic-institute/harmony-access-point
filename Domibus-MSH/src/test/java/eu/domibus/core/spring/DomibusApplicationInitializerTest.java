package eu.domibus.core.spring;

import com.codahale.metrics.servlets.AdminServlet;
import eu.domibus.core.logging.LogbackLoggingConfigurator;
import eu.domibus.core.plugin.classloader.PluginClassLoader;
import eu.domibus.core.property.DomibusConfigLocationProvider;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.DomibusPropertyConfiguration;
import eu.domibus.web.spring.DomibusWebConfiguration;
import mockit.*;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@SuppressWarnings("TestMethodWithIncorrectSignature")
public class DomibusApplicationInitializerTest {

    @Tested
    DomibusApplicationInitializer domibusApplicationInitializer;

    @Test
    public void onStartup(@Injectable ServletContext servletContext,
                          @Mocked DomibusConfigLocationProvider domibusConfigLocationProvider,
                          @Mocked AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext,
                          @Mocked ServletRegistration.Dynamic dispatcher,
                          @Mocked DispatcherServlet dispatcherServlet,
                          @Mocked FilterRegistration.Dynamic springSecurityFilterChain,
                          @Mocked ServletRegistration.Dynamic cxfServlet) throws ServletException, IOException {
        String domibusConfigLocation = "/home/domibus";

        new Expectations(domibusApplicationInitializer) {{
            new DomibusConfigLocationProvider();
            result = domibusConfigLocationProvider;

            domibusConfigLocationProvider.getDomibusConfigLocation(servletContext);
            result = domibusConfigLocation;

            domibusApplicationInitializer.configureLogging(domibusConfigLocation);
            domibusApplicationInitializer.configureMetrics(servletContext);

            new AnnotationConfigWebApplicationContext();
            result = annotationConfigWebApplicationContext;
            times = 2;

            new DispatcherServlet(annotationConfigWebApplicationContext);
            result = dispatcherServlet;


            servletContext.addServlet("dispatcher", dispatcherServlet);
            result = dispatcher;

            domibusApplicationInitializer.configurePropertySources(annotationConfigWebApplicationContext, domibusConfigLocation);

            servletContext.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
            result = springSecurityFilterChain;

            servletContext.addServlet("CXF", CXFServlet.class);

        }};

        domibusApplicationInitializer.onStartup(servletContext);

        new FullVerifications() {{
            annotationConfigWebApplicationContext.register(DomibusRootConfiguration.class);
            annotationConfigWebApplicationContext.register(DomibusWebConfiguration.class);

            List<EventListener> list = new ArrayList<>();
            servletContext.addListener(withCapture(list));
            Assert.assertEquals(2, list.size());
            Assert.assertThat(
                    list.stream().map(EventListener::getClass).collect(Collectors.toList()),
                    CoreMatchers.<Class<?>>hasItems(
                            DomibusContextLoaderListener.class,
                            RequestContextListener.class));

            dispatcher.setLoadOnStartup(1);
            dispatcher.addMapping("/");

            servletContext.setSessionTrackingModes(withAny(new HashSet<>()));
            springSecurityFilterChain.addMappingForUrlPatterns(null, false, "/*");

            cxfServlet.setLoadOnStartup(1);
            cxfServlet.addMapping("/services/*");
        }};
    }

    @Test
    public void onStartup_exception(@Injectable ServletContext servletContext,
                                    @Mocked DomibusConfigLocationProvider domibusConfigLocationProvider,
                                    @Mocked AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext,
                                    @Mocked ServletRegistration.Dynamic dispatcher,
                                    @Mocked DispatcherServlet dispatcherServlet,
                                    @Mocked FilterRegistration.Dynamic springSecurityFilterChain,
                                    @Mocked ServletRegistration.Dynamic cxfServlet) throws IOException {
        String domibusConfigLocation = "/home/domibus";

        new Expectations(domibusApplicationInitializer) {{
            new DomibusConfigLocationProvider();
            result = domibusConfigLocationProvider;

            domibusConfigLocationProvider.getDomibusConfigLocation(servletContext);
            result = domibusConfigLocation;

            domibusApplicationInitializer.configureLogging(domibusConfigLocation);

            new AnnotationConfigWebApplicationContext();
            result = annotationConfigWebApplicationContext;
            times = 1;

            domibusApplicationInitializer.configurePropertySources(annotationConfigWebApplicationContext, domibusConfigLocation);
            result = new IOException("ERROR");

        }};

        try {
            domibusApplicationInitializer.onStartup(servletContext);
            Assert.fail();
        } catch (ServletException e) {
            Assert.assertThat(e.getCause(), CoreMatchers.instanceOf(IOException.class));
        }

        new FullVerifications() {{

            annotationConfigWebApplicationContext.register(DomibusRootConfiguration.class);

        }};
    }

    @Test
    public void configureMetrics(@Mocked ServletContext servletContext,
                                 @Injectable ServletRegistration.Dynamic servlet) {
        new Expectations() {{
            servletContext.addServlet("metrics", AdminServlet.class);
            result = servlet;
        }};

        domibusApplicationInitializer.configureMetrics(servletContext);

        new Verifications() {{
            List<EventListener> list = new ArrayList<>();
            servletContext.addListener(withCapture(list));
            Assert.assertEquals(2, list.size());

            servlet.addMapping("/metrics/*");
            times = 1;
        }};
    }

    @Test
    public void createPluginClassLoader() {
        String domibusConfigLocation = "/home/domibus";

        File pluginsLocation = new File(domibusConfigLocation + DomibusApplicationInitializer.PLUGINS_LOCATION);
        File extensionsLocation = new File(domibusConfigLocation + DomibusApplicationInitializer.EXTENSIONS_LOCATION);

        PluginClassLoader pluginClassLoader = domibusApplicationInitializer.createPluginClassLoader(domibusConfigLocation);

        Assert.assertTrue(pluginClassLoader.getFiles().contains(pluginsLocation));
        Assert.assertTrue(pluginClassLoader.getFiles().contains(extensionsLocation));
    }

    @Test
    public void configureLogging(@Mocked LogbackLoggingConfigurator logbackLoggingConfigurator) {
        String domibusConfigLocation = "/home/domibus";

        domibusApplicationInitializer.configureLogging(domibusConfigLocation);

        new Verifications() {{
            new LogbackLoggingConfigurator(domibusConfigLocation);
            times = 1;

            logbackLoggingConfigurator.configureLogging();
            times = 1;
        }};
    }

    @Test
    public void configurePropertySources(@Injectable AnnotationConfigWebApplicationContext rootContext,
                                         @Injectable ConfigurableEnvironment configurableEnvironment,
                                         @Injectable MutablePropertySources propertySources,
                                         @Injectable MapPropertySource domibusConfigLocationSource,
                                         @Injectable DomibusPropertiesPropertySource domibusPropertiesPropertySource) throws IOException {
        String domibusConfigLocation = "/home/domibus";

        new Expectations(domibusApplicationInitializer) {{
            rootContext.getEnvironment();
            result = configurableEnvironment;

            configurableEnvironment.getPropertySources();
            result = propertySources;

            domibusApplicationInitializer.createDomibusConfigLocationSource(domibusConfigLocation);
            result = domibusConfigLocationSource;

            domibusApplicationInitializer.createDomibusPropertiesPropertySource(domibusConfigLocation);
            result = domibusPropertiesPropertySource;
        }};


        domibusApplicationInitializer.configurePropertySources(rootContext, domibusConfigLocation);

        new Verifications() {{
            propertySources.addFirst(domibusConfigLocationSource);
            times = 1;

            propertySources.addLast(domibusPropertiesPropertySource);
            times = 1;

        }};
    }

    @Test
    public void createDomibusPropertiesPropertySource(@Mocked DomibusPropertyConfiguration domibusPropertyConfiguration,
                                                      @Injectable PropertiesFactoryBean propertiesFactoryBean,
                                                      @Injectable Properties properties,
                                                      @Injectable DomibusPropertiesPropertySource domibusPropertiesPropertySource) throws IOException {
        String domibusConfigLocation = "/home/domibus";

        new Expectations() {{
            new DomibusPropertyConfiguration();
            result = domibusPropertyConfiguration;

            domibusPropertyConfiguration.domibusProperties(domibusConfigLocation);
            result = propertiesFactoryBean;

            propertiesFactoryBean.getObject();
            result = properties;
        }};


        domibusApplicationInitializer.createDomibusPropertiesPropertySource(domibusConfigLocation);

        new Verifications() {{
            propertiesFactoryBean.setSingleton(false);

            new DomibusPropertiesPropertySource(DomibusPropertiesPropertySource.NAME, properties);
            times = 1;
        }};
    }

    @Test
    public void createDomibusConfigLocationSource() {
        String domibusConfigLocation = "/home/domibus";

        MapPropertySource domibusConfigLocationSource = domibusApplicationInitializer.createDomibusConfigLocationSource(domibusConfigLocation);
        Assert.assertEquals(domibusConfigLocationSource.getName(), "domibusConfigLocationSource");
    }
}