package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by the plugins, external modules(DSS) and domibus delegate class to expose configuration property metadata
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
