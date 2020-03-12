package eu.domibus.spring;

import com.codahale.metrics.servlets.AdminServlet;
import eu.domibus.core.property.DomibusConfigLocationProvider;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.property.DomibusPropertyConfiguration;
import eu.domibus.logging.LogbackLoggingConfigurator;
import eu.domibus.plugin.classloader.PluginClassLoader;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusApplicationInitializerTest {

    @Tested
    DomibusApplicationInitializer domibusApplicationInitializer;

    @Test
    public void onStartup(@Injectable ServletContext servletContext,
                          @Mocked DomibusConfigLocationProvider domibusConfigLocationProvider,
                          @Mocked AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext) throws ServletException, IOException {
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

            domibusApplicationInitializer.configurePropertySources(annotationConfigWebApplicationContext, domibusConfigLocation);

        }};

        domibusApplicationInitializer.onStartup(servletContext);

        new Verifications() {{
            List<EventListener> list = new ArrayList<>();
            servletContext.addListener(withCapture(list));
            Assert.assertEquals(2, list.size());
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
    public void createPluginClassLoader() throws MalformedURLException {
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