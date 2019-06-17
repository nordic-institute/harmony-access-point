package eu.domibus.property;

public interface DomibusPropertyChangeListener {

    boolean handlesProperty(String propertyName);

    void propertyValueChanged(String domainCode, String propertyName, String propertyValue);

}
