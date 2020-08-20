package eu.domibus;

import eu.domibus.core.logging.LogbackLoggingConfigurator;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
import eu.domibus.core.spring.DomibusApplicationInitializer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class PropertyOverrideContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyOverrideContextInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment configurableEnvironment = configurableApplicationContext.getEnvironment();

        String domibusConfigLocation = System.getProperty("domibus.config.location");

        configureLogging(domibusConfigLocation);

        DomibusPropertiesPropertySource domibusPropertiesPropertySource = null;
        DomibusPropertiesPropertySource updatedPropertiesPropertySource = null;
        try {
            domibusPropertiesPropertySource = new DomibusApplicationInitializer().createDomibusPropertiesPropertySource(domibusConfigLocation);
            updatedPropertiesPropertySource = new DomibusApplicationInitializer().createUpdatedDomibusPropertiesSource();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        propertySources.addLast(domibusPropertiesPropertySource);
        propertySources.addFirst(updatedPropertiesPropertySource);
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
}