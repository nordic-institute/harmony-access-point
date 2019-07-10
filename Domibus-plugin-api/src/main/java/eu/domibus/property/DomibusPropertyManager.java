package eu.domibus.property;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * The interface implemented by MSH and the plugins to handle getting and setting of domibus properties at runtime
 */
public interface DomibusPropertyManager {

    /**
     *  Get all the properties metadata that support changing at runtime
     * @return properties as metadata
     */
    Map<String, DomibusPropertyMetadata> getKnownProperties();

    /**
     * True if the manager handles the specified property
     * @param name the name of the property
     * @return
     */
    boolean hasKnownProperty(String name);

    /**
     * Returns the current property value
     * @param domainCode the domain on which the property value is requested
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    String getKnownPropertyValue(String domainCode, String propertyName);

    /**
     * Replaces/Sets the current property value
     * @param domainCode the domain on which the property value is set
     * @param propertyName the property name whose value is set
     * @param propertyValue the new property value
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue);

    /**
     * Replaces/Sets the current property value
     * @param domainCode the domain on which the property value is set
     * @param propertyName the property name whose value is set
     * @param propertyValue the new property value
     * @param broadcast Specifies if the property change needs to be broadcasted to all nodes in the cluster
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast);
}
