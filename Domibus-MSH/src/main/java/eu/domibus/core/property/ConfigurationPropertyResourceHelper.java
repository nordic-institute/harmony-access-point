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
     * Retrieves all properties from MSH and plugins filtered by the parameters
     *
     * @param name part of the property name, case insensitive
     * @param showDomainProperties domain or global properties
     * @param type the type of the property
     * @param module the module of the property
     * @param value the value of the property
     * @return the list of the properties: metadata and value
     */
    List<DomibusProperty> getAllWritableProperties(String name, boolean showDomainProperties, String type, String module, String value);

    /**
     * Sets the property with specified name to the specified value
     *
     * @param name  name of the property
     * @param value the new value
     * @param isDomain the domain of global property
     * */
    void setPropertyValue(String name, boolean isDomain, String value) throws DomibusPropertyException;

    /**
     * Retrieves the property metadata along with its current value
     * @param propertyName the name of the property
     * @return Metadata and the current value
     */
    DomibusProperty getProperty(String propertyName);
}
