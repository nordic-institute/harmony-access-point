package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.services.DomibusPropertyManagerExt;

import java.util.Map;

public interface GlobalPropertyMetadataManager {

    /**
     * Returns all properties metadata, internal and from external modules
     *
     * @return Map<String, DomibusPropertyMetadata> the map of property metadata
     */
    Map<String, DomibusPropertyMetadata> getAllProperties();

    /**
     * Returns the metadata for a given propertyName by interrogating all property managers known to Domibus in order to find it.
     * If not found, it checks for a possible composable property and creates if it is
     * If still not found, it assumes it is a global property and it creates the corresponding metadata on-the-fly.
     *
     * @param propertyName
     * @return DomibusPropertyMetadata
     */
    DomibusPropertyMetadata getPropertyMetadata(String propertyName);

    /**
     * Determines if a property is managed internally by the domibus property provider or by an external property manager (like dss or plugins)
     *
     * @param propertyName the name of the property
     * @return null in case it is an internal property; external module property manager, in case of an external property
     * @throws DomibusPropertyException in case the property is not found anywhere
     */
    DomibusPropertyManagerExt getManagerForProperty(String propertyName) throws DomibusPropertyException;

    /**
     * Checks if a domibus property exists with the exact specified name or a composable property that matches and has a value defined
     *
     * @param propertyName the name of the propertyto check
     * @return true if it exists; otherwise false
     */
    boolean hasKnownProperty(String propertyName);

    /**
     * Checks if the name represents a composable type of property in a broad sense (any suffix is accepted) and returns the source/parent metadata
     *
     * @param propertyName the name
     * @return the source metadata, if composable; otherwise null
     */
    DomibusPropertyMetadata getComposableProperty(String propertyName);
}
