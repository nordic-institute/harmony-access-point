package eu.domibus.core.alerts.model.service;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class EventProperties {
    Object[] properties;

    public EventProperties(Object... properties) {
        this.properties = properties;
    }

    public Object[] getProperties() {
        return properties;
    }

}
