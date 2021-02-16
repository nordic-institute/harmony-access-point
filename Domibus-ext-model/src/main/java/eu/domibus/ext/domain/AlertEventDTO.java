package eu.domibus.ext.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class AlertEventDTO {

    private AlertLevelDTO alertLevelDTO;
    private Map<String, String> properties = new HashMap<>(); //NOSONAR

    public AlertEventDTO(AlertLevelDTO alertLevelDTO) {
        this.alertLevelDTO = alertLevelDTO;
    }

    public AlertLevelDTO getAlertLevelDTO() {
        return alertLevelDTO;
    }

    public void setAlertLevelDTO(AlertLevelDTO alertLevelDTO) {
        this.alertLevelDTO = alertLevelDTO;
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
