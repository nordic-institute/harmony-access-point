package eu.domibus.core.property;

import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusPropertyConfigurationTest {

    @Tested
    DomibusPropertyConfiguration domibusPropertyConfiguration;

    @Test
    public void domibusDefaultProperties(@Mocked PropertiesFactoryBean propertiesFactoryBean) throws IOException {

        domibusPropertyConfiguration.domibusDefaultProperties();

        new Verifications() {{
            Resource[] locations = null;
            propertiesFactoryBean.setLocations(locations = withCapture());

            Assert.assertEquals("application.properties", locations[0].getFilename());
            Assert.assertEquals("domibus-default.properties", locations[1].getFilename());
            Assert.assertEquals("domibus.properties", locations[2].getFilename());
        }};

    }

    @Test
    public void domibusProperties(@Mocked PropertiesFactoryBean propertiesFactoryBean) throws IOException {
        String domibusConfigLocation = "configLocation";

        domibusPropertyConfiguration.domibusProperties(domibusConfigLocation);

        new Verifications() {{
            Resource[] locations = null;
            propertiesFactoryBean.setLocations(locations = withCapture());

            Assert.assertEquals("application.properties", locations[0].getFilename());
            Assert.assertEquals("domibus-default.properties", locations[1].getFilename());
            Assert.assertEquals("domibus.properties", locations[2].getFilename());
            Assert.assertEquals("/" + domibusConfigLocation + "/domibus.properties", locations[3].getFile().getPath());
        }};
    }

    @Test
    public void propertySourcesPlaceholderConfigurer(@Mocked PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer) {
        domibusPropertyConfiguration.propertySourcesPlaceholderConfigurer();

        new Verifications() {{
            Boolean ignoreResourceNotFound = null;
            Boolean ignoreUnresolvablePlaceholders = null;
            propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(ignoreResourceNotFound = withCapture());
            propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(ignoreUnresolvablePlaceholders = withCapture());

            Assert.assertEquals(true, ignoreResourceNotFound);
            Assert.assertEquals(true, ignoreUnresolvablePlaceholders);
        }};
    }
}