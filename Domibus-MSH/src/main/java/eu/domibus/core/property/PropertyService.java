package eu.domibus.core.property;

import eu.domibus.api.property.Property;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public interface PropertyService {
    List<Property> getProperties(String name);

    void setPropertyValue(String name, String value);
}
