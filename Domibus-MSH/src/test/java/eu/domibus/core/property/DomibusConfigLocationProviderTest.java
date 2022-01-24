package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import javax.servlet.ServletContext;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(MockitoJUnitRunner.class)
public class DomibusConfigLocationProviderTest {

    @Tested
    DomibusConfigLocationProvider domibusConfigLocationProvider;

    @Injectable
    ServletContext servletContext;

    @Test
    public void getDomibusConfigLocationWithServletInitParameterConfigured() {
        String domibusConfigLocationInitParameter = "servletConfigLocation";
        new Expectations() {{
            servletContext.getInitParameter(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
            result = domibusConfigLocationInitParameter;
        }};

        Assert.assertEquals(domibusConfigLocationInitParameter, domibusConfigLocationProvider.getDomibusConfigLocation(servletContext));
    }

    @Test
    public void getDomibusConfigLocation(@Mocked System system) {
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