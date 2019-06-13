package eu.domibus.core.property;

import eu.domibus.web.rest.ro.PropertyRO;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public interface PropertyService {
    List<PropertyRO> getProperties(String name);
}
