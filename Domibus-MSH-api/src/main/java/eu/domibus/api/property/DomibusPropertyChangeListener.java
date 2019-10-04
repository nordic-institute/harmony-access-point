package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Implemented by classes that execute specific code to handle the property value change
 */
public interface DomibusPropertyChangeListener {

    /**
     * If the class handles the property
     * @param propertyName
     * @return
     */
    boolean handlesProperty(String propertyName);

    /**
     * The code that handles the change of the property value
     * @param domainCode the domain on which the property is changed
     * @param propertyName the name of the property whose value has changed
     * @param propertyValue the newly set value
     */
    void propertyValueChanged(String domainCode, String propertyName, String propertyValue);

}
