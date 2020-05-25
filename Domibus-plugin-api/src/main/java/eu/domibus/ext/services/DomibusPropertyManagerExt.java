package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by the plugins, external modules(DSS) and domibus msh delegate
 * Used to expose all (configuration) property metadata and for getting and setting properties at runtime
 */
public interface DomibusPropertyManagerExt extends DomibusPropertyMetadataManagerExt {

    /**
     * Returns the current property value for the current domain
     *
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    String getKnownPropertyValue(String propertyName);

    /**
     * Replaces/Sets the current property value in the current domain
     *
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     */
    void setKnownPropertyValue(String propertyName, String propertyValue);

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     * @param broadcast     Specifies if the property change needs to be broadcasted to all nodes in the cluster
     */
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast);

    /**
     * Returns the current property value
     *
     * @param domainCode   the domain on which the property value is requested
     * @param propertyName the property name whose value is requested
     * @return the current property value
     */
    /**
     * @deprecated Use instead {@link eu.domibus.ext.services.DomibusPropertyManagerExt#getKnownPropertyValue(java.lang.String) }
     */
    @Deprecated
    String getKnownPropertyValue(String domainCode, String propertyName);

    /**
     * Replaces/Sets the current property value
     *
     * @param domainCode    the domain on which the property value is set
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     */
    /**
     * @deprecated Use instead {@link eu.domibus.ext.services.DomibusPropertyManagerExt#setKnownPropertyValue(java.lang.String, java.lang.String) }
     */
    @Deprecated
    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue);
}
