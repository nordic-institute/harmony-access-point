package eu.domibus.ext.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class AlertEventDTOBuilder {

    private AlertLevelDTO alertLevelDTO;

    private String name;

    private boolean active;

    private String emailSubject;

    private String emailBody;

    private Map<String, String> properties = new HashMap<>(); //SONAR

    public static AlertEventDTOBuilder getInstance() {
        return new AlertEventDTOBuilder();
    }

    public AlertEventDTO build() {
        return new AlertEventDTO(alertLevelDTO, name, active, emailSubject, emailBody, properties);
    }

    public AlertEventDTOBuilder alertLevelDTO(AlertLevelDTO alertLevelDTO) {
        this.alertLevelDTO = alertLevelDTO;
        return this;
    }

    public AlertEventDTOBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AlertEventDTOBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public AlertEventDTOBuilder emailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
        return this;
    }

    public AlertEventDTOBuilder emailBody(String emailBody) {
        this.emailBody = emailBody;
        return this;
    }

    public AlertEventDTOBuilder properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

}