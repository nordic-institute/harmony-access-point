package eu.domibus.core.property;

import eu.domibus.api.property.DomibusProperty;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Service used by PropertyResource, responsible with getting and setting of domibus props that can be changed at runtime
 */
public interface DomibusPropertyService {

    /**
     * Retrieves all properties from MSH and plugins that contains this string in its name
     *
     * @param name the filter value
     * @return
     */
    List<DomibusProperty> getProperties(String name);

    /**
     * Stets the property with specified name to the specified value
     *
     * @param name  name of the property
     * @param value the new value
     */
    void setPropertyValue(String name, String value);

}
