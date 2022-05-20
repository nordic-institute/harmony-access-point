package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * Class responsible for configuring Domibus properties sources
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusPropertiesPropertySource extends PropertiesPropertySource {

    public static final String NAME = "domibusProperties";
    public static final String UPDATED_PROPERTIES_NAME = "updatedDomibusProperties";

    public DomibusPropertiesPropertySource(String name, Properties source) {
        super(name, source);
    }

    protected DomibusPropertiesPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    public void setProperty(String name, String value) {
        // cannot set null in the map!
        if (value == null) {
            // or we can delete the property entry altogether if we so decide!
            throw new DomibusPropertyException("Cannot set a null value for a property; use an empty value instead.");
        }
        this.source.put(name, value);
    }
}
