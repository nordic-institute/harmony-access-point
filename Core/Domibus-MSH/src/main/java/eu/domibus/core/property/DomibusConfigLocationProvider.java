package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusConfigLocationProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusConfigLocationProvider.class);

    public String getDomibusConfigLocation(ServletContext servletContext) {
        String domibusConfigLocationInitParameter = servletContext.getInitParameter(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
        if (StringUtils.isNotBlank(domibusConfigLocationInitParameter)) {
            LOG.info("Property [{}] is configured as a servlet init parameter with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION, domibusConfigLocationInitParameter);
            return domibusConfigLocationInitParameter;
        }

        String domibusConfigLocation = System.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION);
        if (StringUtils.isNotBlank(domibusConfigLocation)) {
            LOG.info("Property [{}] is configured as a system property with [{}]", DomibusPropertyMetadataManagerSPI.DOMIBUS_CONFIG_LOCATION, domibusConfigLocation);
            return domibusConfigLocation;
        }
        return null;
    }

    public String getDomibusExtensionsLocation(ServletContext servletContext) {
        String location = servletContext
                .getInitParameter(DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSIONS_LOCATION);

        if ( location == null ) {
            location = System.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSIONS_LOCATION);
        }

        if ( location == null ) {
            location = getDomibusConfigLocation(servletContext);
        }

        LOG.info("Property [{}] is configured as [{}]",
                DomibusPropertyMetadataManagerSPI.DOMIBUS_EXTENSIONS_LOCATION, location);

        return location;
    }
}
