package eu.domibus;

import eu.domibus.core.spring.DomibusApplicationInitializer;
import eu.domibus.core.property.DomibusPropertiesPropertySource;
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

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment configurableEnvironment = configurableApplicationContext.getEnvironment();

        String domibusConfigLocation = System.getProperty("domibus.config.location");
        DomibusPropertiesPropertySource domibusPropertiesPropertySource = null;
        try {
            domibusPropertiesPropertySource = new DomibusApplicationInitializer().createDomibusPropertiesPropertySource(domibusConfigLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        propertySources.addLast(domibusPropertiesPropertySource);
    }
}