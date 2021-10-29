package eu.domibus.ext.services;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.DomainDTO;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

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
     * @param domain       The domain property
     * @param propertyName The property name for which the value is retrieved
     * @return The property value
     */
    default String getProperty(DomainDTO domain, String propertyName) {
        return null;
    }

    /**
     * Gets the integer property value with the provided name
     *
     * @param propertyName The property name for which the integer value is retrieved
     * @return The property value as Integer
     */
    default Integer getIntegerProperty(String propertyName) {
        return null;
    }

    /**
     * Gets the boolean property value with the provided name
     *
     * @param propertyName The property name for which the integer value is retrieved
     * @return The property value as Boolean
     */
    default Boolean getBooleanProperty(String propertyName) {
        return null;
    }

    /**
     * Returns all property names for which the given predicate is true
     *
     * @param predicate the predicate to filter with
     * @return A set of property names
     */
    default Set<String> filterPropertiesName(Predicate<String> predicate) {
        return null;
    }

    /**
     * Returns the list of nested properties names starting with the specified prefix
     * <p/>
     * Eg. Given the properties routing.rule1=Rule1 name, routing.rule1.queue=jms.queue1, routing.rule2=Rule2 name, routing.rule2.queue=jms.queue2
     * it will return for the prefix "routing" the following list : rule1, rule1.queue, rule2, rule2.queue
     *
     * @param prefix The nested properties prefix
     * @return the list of nested properties
     */
    default List<String> getNestedProperties(String prefix) {
        return null;
    }

    /**
     * Get the list of configured message notifications, separated by comma, configured in the plugin property file
     *
     * @param notificationPropertyName The property name
     * @return the list of message notifications
     */
    default List<NotificationType> getConfiguredNotifications(String notificationPropertyName) {
        return null;
    }

    /**
     * Sets the property value with the provided name for the current domain
     *
     * @param propertyName  The property name for which the value is updated
     * @param propertyValue The new property value
     */
    default void setProperty(String propertyName, String propertyValue) {
    }

    /**
     * Verify that a property key exists within a domain configuration whether it is empty or not.
     * If not found, the property will be looked within the domibus/default-domain properties
     *
     * @param domain       the domain.
     * @param propertyName the property name.
     * @return true if the property exists.
     */
    default boolean containsDomainPropertyKey(DomainDTO domain, String propertyName) {
        return false;
    }

    /**
     * Verify that a property key exists within the domibus/default-domain properties.
     *
     * @param propertyName the name of the property
     * @return true if the property exists.
     */
    default boolean containsPropertyKey(String propertyName) {
        return false;
    }


    /**
     * Sets the property value with the provided name for a specific domain
     *
     * @param domain        The domain on which to set the property value
     * @param propertyName  The property name for which the value is updated
     * @param propertyValue The new property value
     * @param broadcast     If the new value should be broadcasted to all nodes in the cluster
     **/
    default void setProperty(DomainDTO domain, String propertyName, String propertyValue, boolean broadcast) {
    }

    void loadProperties(DomainDTO domain, String propertiesFilePath);
}
