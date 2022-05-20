package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * This notifies the custom action listeners and the other nodes in a cluster environment.
 */
public interface DomibusPropertyChangeNotifier {

    /**
     * Dispatch the property change notification to the interested listeners.
     * @param domainCode the domain on which the property is changed
     * @param propertyName the name of the property whose value has changed
     * @param propertyValue the newly set value
     * @param broadcast whether the other cluster nodes need to be notified
     */
    void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast) throws DomibusPropertyException;
}
