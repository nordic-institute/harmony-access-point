package eu.domibus.api.alerts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class AlertEvent {
    private AlertLevel alertLevel;
    private Map<String, String> properties = new HashMap<>(); //NOSONAR

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    void addProperty(String key, String value) {
        this.properties.put(key, value);
    }
}
