package eu.domibus.core.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyProvider.DOMIBUS_PROPERTY_FILE;
import static eu.domibus.ext.services.DomibusPropertyManagerExt.*;

/**
 * Class responsible for configuring Domibus property sources in a specific order
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration("domibusPropertyConfiguration")
public class DomibusPropertyConfiguration {

    public static final String MULTITENANT_DOMIBUS_PROPERTIES_SUFFIX = "-domibus.properties";
    public static final String SUPER_DOMIBUS_PROPERTIES = "super" + MULTITENANT_DOMIBUS_PROPERTIES_SUFFIX;
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyConfiguration.class);

    @Bean("domibusDefaultProperties")
    public PropertiesFactoryBean domibusDefaultProperties() throws IOException {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setIgnoreResourceNotFound(true);
        result.setFileEncoding(StandardCharsets.UTF_8.name());

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/domibus-default.properties"));
        resources.add(new ClassPathResource("config/" + DOMIBUS_PROPERTY_FILE));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] pluginDefaultResourceList = resolver.getResources("classpath*:config/*-plugin-default.properties");
        LOG.debug("Adding the following plugin default properties files [{}]", pluginDefaultResourceList);
        resources.addAll(Arrays.asList(pluginDefaultResourceList));

        result.setLocations(resources.toArray(new Resource[0]));
        return result;
    }

    public PropertiesFactoryBean domibusProperties(String domibusConfigLocation) throws IOException {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setIgnoreResourceNotFound(true);
        result.setFileEncoding(StandardCharsets.UTF_8.name());

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/domibus-default.properties"));
        resources.add(new ClassPathResource("config/" + DOMIBUS_PROPERTY_FILE));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource domibusProperties = resolver.getResource("file:///" + domibusConfigLocation + "/" + DOMIBUS_PROPERTY_FILE);
        resources.add(domibusProperties);

        Resource superProperties = resolver.getResource("file:///" + domibusConfigLocation + File.separator + DOMAINS_HOME + File.separator + SUPER_DOMIBUS_PROPERTIES);
        if (superProperties.exists()) {
            LOG.debug("Adding the super properties file [{}]", superProperties);
            resources.add(superProperties);
        }

        Resource[] domainProperties = resolver.getResources("file:///" + domibusConfigLocation + File.separator + DOMAINS_HOME + "/*/*" + MULTITENANT_DOMIBUS_PROPERTIES_SUFFIX);
        LOG.debug("Adding the following domain properties files [{}]", domainProperties);
        resources.addAll(Arrays.asList(domainProperties));

        Resource[] pluginDefaultResourceList = resolver.getResources("classpath*:config/*-plugin-default.properties");
        LOG.debug("Adding the following plugin default properties files [{}]", pluginDefaultResourceList);
        resources.addAll(Arrays.asList(pluginDefaultResourceList));

        Resource[] pluginResourceList = resolver.getResources("file:///" + domibusConfigLocation + File.separator + PLUGINS_CONFIG_HOME + "/*-plugin.properties");
        LOG.debug("Adding the following plugin properties files [{}]", pluginResourceList);
        resources.addAll(Arrays.asList(pluginResourceList));

        Resource[] domainPluginResourceList = resolver.getResources("file:///" + domibusConfigLocation + File.separator + PLUGINS_CONFIG_HOME
                + File.separator + DOMAINS_HOME + "/*/*-plugin.properties");
        LOG.debug("Adding the following domain plugin properties files [{}]", domainPluginResourceList);
        resources.addAll(Arrays.asList(domainPluginResourceList));

        Resource[] extensionResourceList = resolver.getResources("file:///" + domibusConfigLocation + File.separator + EXTENSIONS_CONFIG_HOME + "/*-extension.properties");
        LOG.debug("Adding the following extension properties files [{}]", extensionResourceList);
        resources.addAll(Arrays.asList(extensionResourceList));

        Resource[] domainExtensionResourceList = resolver.getResources("file:///" + domibusConfigLocation + File.separator + EXTENSIONS_CONFIG_HOME
                + File.separator + DOMAINS_HOME + "/*/*-extension.properties");
        LOG.debug("Adding the following domain extension properties files [{}]", domainExtensionResourceList);
        resources.addAll(Arrays.asList(domainExtensionResourceList));

        result.setLocations(resources.toArray(new Resource[0]));
        return result;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
        result.setFileEncoding(StandardCharsets.UTF_8.name());
        result.setIgnoreResourceNotFound(true);
        result.setIgnoreUnresolvablePlaceholders(true);
        return result;
    }
}
