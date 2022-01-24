package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class DomibusConfigLocationProviderTest {

    @Tested
    DomibusConfigLocationProvider domibusConfigLocationProvider;

    @Test
    public void getDomibusConfigLocationWithServletInitParameterConfigured(@Injectable ServletContext servletContext) {
        String domibusConfigLocationInitParameter = "servletConfigLocation";
        new Expectations() {{
            servletContext.getInitParameter(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
            result = domibusConfigLocationInitParameter;
        }};

        Assert.assertEquals(domibusConfigLocationInitParameter, domibusConfigLocationProvider.getDomibusConfigLocation(servletContext));
    }

    @Test
    public void getDomibusConfigLocation(@Injectable ServletContext servletContext,
                                         @Mocked System system) {
        String systemConfigLocation = "systemConfigLocation";
        new Expectations() {{
            servletContext.getInitParameter(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
            result = null;

            system.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
            result = systemConfigLocation;
        }};

        Assert.assertEquals(systemConfigLocation, domibusConfigLocationProvider.getDomibusConfigLocation(servletContext));
    }
}