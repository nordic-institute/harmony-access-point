package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * Responsible for property related operations
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomibusPropertyExtService {

    /**
     * Gets the property value with the provided name
     *
     * @param propertyName The property name for which the value is retrieved
     * @return The property value
     */
    String getProperty(String propertyName);

    /**
     * Gets the property value with the provided name for a specific domain
     *
     * @param domain The domain property
     * @param propertyName The property name for which the value is retrieved
     * @return The property value
     */
    String getDomainProperty(DomainDTO domain, String propertyName);

    /**
     * Verify that a property key exists within a domain configuration whether it is empty or not.
     * If not found, the property will be looked within the domibus/default-domain properties
     *
     * @param domain       the domain.
     * @param propertyName the property name.
     * @return true if the property exists.
     */
    boolean containsDomainPropertyKey(DomainDTO domain, String propertyName);

    /**
     * Verify that a property key exists within the domibus/default-domain properties.
     * @param propertyName the name of the property
     * @return true if the property exists.
     */
    boolean containsPropertyKey(String propertyName);

    /**
     * Gets the property value with the provided name for a specific domain
     *
     * @param domain The domain property
     * @param propertyName The property name for which the value is retrieved
     * @param defaultValue The default value to return in case the property value is not found
     * @return The property value
     */
    String getDomainProperty(DomainDTO domain, String propertyName, String defaultValue);

    /**
     * @deprecated Use instead {@link eu.domibus.ext.services.DomibusPropertyExtService#getDomainProperty(eu.domibus.ext.domain.DomainDTO, java.lang.String) }
     */
    @Deprecated
    String getDomainResolvedProperty(DomainDTO domain, String propertyName);

    /**
     * @deprecated Use instead {@link DomibusPropertyExtService#getProperty(java.lang.String) }
     */
    @Deprecated
    String getResolvedProperty(String propertyName);

}
