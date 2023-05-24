package eu.domibus.plugin.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * The plugin api equivalent of DomibusPropertyChangeNotifier
 * This notifies the custom action listeners and the other nodes in a cluster environment.
 *
 *  Currently this interface is not used since all default plugins delegate to domibus property provider notification mechanism
 */
public interface PluginPropertyChangeNotifier {

    /**
     * Dispatch the property change notification to the interested listeners.
     * @param domainCode the domain on which the property is changed
     * @param propertyName the name of the property whose value has changed
     * @param propertyValue the newly set value
     * @param broadcast whether the clusted nodes need to be notified
     */
    void signalPropertyValueChanged(String domainCode, String propertyName, String propertyValue, boolean broadcast);
}
