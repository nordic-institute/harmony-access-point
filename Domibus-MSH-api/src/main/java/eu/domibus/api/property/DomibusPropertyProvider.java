package eu.domibus.api.property;

import eu.domibus.api.multitenancy.Domain;

import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Cosmin Baciu
 * @author Ion Perpegel
 * @since 4.0
 */
public interface DomibusPropertyProvider {

    String DOMIBUS_PROPERTY_FILE = "domibus.properties";

    /**
     * Retrieves the property value, taking into account the property usages and the current domain.
     * If needed, it falls back to the default value provided in the global properties set.
     */
    String getProperty(String propertyName) throws DomibusPropertyException;

    /**
     * Retrieves the property value from the requested domain. If not found, fall back to the property value from the global properties set.
     *
     * @param domain       the domain.
     * @param propertyName the property name.
     * @return the value for that property.
     */
    String getProperty(Domain domain, String propertyName) throws DomibusPropertyException;

    /**
     * Returns all property names for which the given predicate is true
     *
     * @param predicate the predicate to filter with
     * @return A set of property names
     */
    Set<String> filterPropertiesName(Predicate<String> predicate);

    /**
     * <p>Reads a property value and parses it safely as an {@code Integer} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     * @return The {@code Integer} value of the property as specified by the user or the default one provided with the application.
     */
    Integer getIntegerProperty(String propertyName);

    /**
     * <p>Reads the property value and parses it safely as an {@code Long} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     * @return The {@code Long} value of the property as specified by the user or the default one provided with the application.
     */
    Long getLongProperty(String propertyName);

    /**
     * <p>Reads a property value and parses it safely as a {@code Boolean} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     * @return The {@code Boolean} value of the property as specified by the user or the default one provided with the application.
     */
    Boolean getBooleanProperty(String propertyName);

    /**
     * <p>Reads a domain property value and parses it safely as a {@code Boolean} before returning it.</p><br />
     *
     * <p>If the value is not found in the users files, the default value is then being returned from the domibus-default.properties and its corresponding server-specific
     * domibus.properties files that are provided with the application.</p>
     *
     * @param propertyName the property name.
     * @param domain       the domain.
     * @return The {@code Boolean} value of the domain property as specified by the user or the default one provided with the application.
     */
    Boolean getBooleanProperty(Domain domain, String propertyName);

    /**
     * Verify that a property key exists within a domain configuration whether it is empty or not.
     * If not found, the property will be looked within the domibus/default-domain properties
     *
     * @param domain       the domain.
     * @param propertyName the property name.
     * @return true if the property exists.
     */
    boolean containsDomainPropertyKey(Domain domain, String propertyName);

    /**
     * Verify that a property key exists within the domibus/default-domain properties.
     *
     * @param propertyName the name of the property
     * @return true if the property exists.
     */
    boolean containsPropertyKey(String propertyName);

    /**
     * Replaces/Sets the current property value in the current domain
     * In case the value cannot be set because the property change listener fails, the DomibusPropertyException is raised
     *
     * @param propertyName  the property name whose value is set
     * @param propertyValue the new property value
     * @throws DomibusPropertyException in case the value cannot be set because the property change listener fails
     */
    void setProperty(String propertyName, String propertyValue) throws DomibusPropertyException;

    /**
     * Sets a new property value for the given property, in the given domain.
     * In case the value cannot be set because the property change listener fails or if the domain is null, the DomibusPropertyException is raised
     *
     * @param domain        the domain of the property
     * @param propertyName  the name of the property
     * @param propertyValue the new value of the property
     * @throws DomibusPropertyException in case the value cannot be set because the property change listener fails
     */
    void setProperty(Domain domain, String propertyName, String propertyValue) throws DomibusPropertyException;

    /**
     * Sets a new property value for the given property, in the given domain.
     * In case the value cannot be set because the property change listener fails or if the domain is null, the DomibusPropertyException is raised
     *
     * @param domain        the domain of the property
     * @param propertyName  the name of the property
     * @param propertyValue the new value of the property
     * @param broadcast     Specifies if the property change needs to be broadcasted to all nodes in the cluster
     * @throws DomibusPropertyException in case the value cannot be set because the property change listener fails or if the domain is null
     */
    void setProperty(Domain domain, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException;
}
