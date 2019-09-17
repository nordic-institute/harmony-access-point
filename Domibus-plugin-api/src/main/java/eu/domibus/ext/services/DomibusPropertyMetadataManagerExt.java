package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by the plugins and Domibus delegate class to handle getting and setting of domibus properties at runtime
 */
public interface DomibusPropertyMetadataManagerExt {

    /**
     * Get all the properties metadata that support changing at runtime
     *
     * @return properties as metadata
     */
    Map<String, DomibusPropertyMetadataDTO> getKnownProperties();

    /**
     * True if the manager handles the specified property
     *
     * @param name the name of the property
     * @return
     */
    boolean hasKnownProperty(String name);
}
