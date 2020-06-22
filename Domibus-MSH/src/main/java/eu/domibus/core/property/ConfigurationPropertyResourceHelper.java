package eu.domibus.core.property;

import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Service used by ConfigurationPropertyResource, responsible with getting and setting of domibus props that can be changed at runtime
 */
public interface ConfigurationPropertyResourceHelper {

    /**
     * Retrieves all properties from MSH and plugins that contains this string in its name
     *
     * @param name the filter value
     * @return
     */
    List<DomibusProperty> getAllWritableProperties(String name, boolean showDomainProperties);

    /**
     * Sets the property with specified name to the specified value
     *
     * @param name  name of the property
     * @param value the new value
     */
    void setPropertyValue(String name, boolean isDomain, String value) throws DomibusPropertyException;

    /**
     * Retrieves the property metadata along with its current value
     * @param propertyName the name of the property
     * @return Metadata and the current value
     */
    DomibusProperty getProperty(String propertyName);
}
