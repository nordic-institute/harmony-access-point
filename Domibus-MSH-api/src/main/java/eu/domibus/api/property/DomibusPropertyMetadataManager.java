package eu.domibus.api.property;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by MSH and the plugins to handle getting and setting of domibus properties at runtime
 */
public interface DomibusPropertyMetadataManager {

    /**
     * Get all the properties metadata that support changing at runtime
     *
     * @return properties as metadata
     */
    Map<String, DomibusPropertyMetadata> getKnownProperties();

    /**
     * True if the manager handles the specified property
     *
     * @param name the name of the property
     * @return
     */
    boolean hasKnownProperty(String name);
}
