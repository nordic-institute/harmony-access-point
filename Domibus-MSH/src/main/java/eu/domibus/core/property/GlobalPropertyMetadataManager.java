package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.services.DomibusPropertyManagerExt;

import java.util.Map;

public interface GlobalPropertyMetadataManager {

    /**
     *  Returns all properties metadata, internal and from external modules
     * @return Map<String, DomibusPropertyMetadata> the map of property metadata
     */
    Map<String, DomibusPropertyMetadata> getAllProperties();

    /**
     * Returns the metadata for a given propertyName by interrogating all property managers known to Domibus in order to find it.
     * If not found, it assumes it is a global property and it creates the corresponding metadata on-the-fly.
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
}
