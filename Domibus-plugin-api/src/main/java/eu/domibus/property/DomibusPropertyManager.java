package eu.domibus.property;

import java.util.Map;

public interface DomibusPropertyManager {

    Map<String, DomibusPropertyMetadata> getKnownProperties();

    boolean hasKnownProperty(String name);

    String getKnownPropertyValue(String domainCode, String propertyName);

    void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue);

    //TODO: probably not the ideal place for it
    //void handlePropertyChange(String domainCode, String propertyName, String propertyValue);
}
