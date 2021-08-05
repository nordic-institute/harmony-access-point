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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class responsible for configuring Domibus property sources in a specific order
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration("domibusPropertyConfiguration")
public class DomibusPropertyConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyConfiguration.class);

    @Bean("domibusDefaultProperties")
    public PropertiesFactoryBean domibusDefaultProperties() throws IOException {
        PropertiesFactoryBean result = new PropertiesFactoryBean();
        result.setIgnoreResourceNotFound(true);

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/domibus-default.properties"));
        resources.add(new ClassPathResource("config/domibus.properties"));

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

        List<Resource> resources = new ArrayList<>();
        resources.add(new ClassPathResource("config/domibus-default.properties"));
        resources.add(new ClassPathResource("config/domibus.properties"));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource domibusProperties = resolver.getResource("file:///" + domibusConfigLocation + "/domibus.properties");
        resources.add(domibusProperties);

        Resource[] domainProperties = resolver.getResources("file:///" + domibusConfigLocation + "/*-domibus.properties");
        LOG.debug("Adding the following domain properties files [{}]", domainProperties);
        resources.addAll(Arrays.asList(domainProperties));

        Resource[] pluginDefaultResourceList = resolver.getResources("classpath*:config/*-plugin-default.properties");
        LOG.debug("Adding the following plugin default properties files [{}]", pluginDefaultResourceList);
        resources.addAll(Arrays.asList(pluginDefaultResourceList));

        Resource[] pluginResourceList = resolver.getResources("file:///" + domibusConfigLocation + "/plugins/config/*-plugin.properties");
        LOG.debug("Adding the following plugin properties files [{}]", pluginResourceList);
        resources.addAll(Arrays.asList(pluginResourceList));

        Resource[] extensionResourceList = resolver.getResources("file:///" + domibusConfigLocation + "/extensions/config/*-extension.properties");
        LOG.debug("Adding the following extension properties files [{}]", extensionResourceList);
        resources.addAll(Arrays.asList(extensionResourceList));

        result.setLocations(resources.toArray(new Resource[0]));
        return result;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
        result.setIgnoreResourceNotFound(true);
        result.setIgnoreUnresolvablePlaceholders(true);
        return result;
    }
}
