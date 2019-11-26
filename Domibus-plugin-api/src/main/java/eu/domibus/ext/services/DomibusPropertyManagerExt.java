package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by the plugins and Domibus delegate class to handle getting and setting of domibus properties at runtime
 */
public interface DomibusPropertyManagerExt extends DomibusPropertyMetadataManagerExt {

    /**
     * Returns the current property value
     *
     * @param domainCode   the domain on which the property value is requested
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    String getKnownPropertyValue(String domainCode, String propertyName);

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue);

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     * @param broadcast     Specifies if the property change needs to be broadcasted to all nodes in the cluster
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast);
}
