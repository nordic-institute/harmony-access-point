package eu.domibus.core.property;

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
        this.source.put(name, value);
    }
}
