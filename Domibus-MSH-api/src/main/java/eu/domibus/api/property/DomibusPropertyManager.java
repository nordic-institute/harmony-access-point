package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by MSH and server specific managers, to expose all property metadata and handle getting and setting them at runtime
 */
public interface DomibusPropertyManager extends DomibusPropertyMetadataManager {

    public static final String MSH_PROPERTY_MANAGER = "mshPropertyManager";

    /**
     * Returns the current property value
     *
     * @param domainCode   the domain on which the property value is requested
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    String getKnownPropertyValue(String domainCode, String propertyName);

    /**
     * Returns the current property value for the current domain
     *
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    String getKnownPropertyValue(String propertyName);

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyException;

    /**
     * Replaces/Sets the current property value in the current domain
     *
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     */
    void setKnownPropertyValue(String propertyName, String propertyValue) throws DomibusPropertyException;

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     * @param broadcast     Specifies if the property change needs to be broadcasted to all nodes in the cluster
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException;
}
