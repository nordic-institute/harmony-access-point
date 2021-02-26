package eu.domibus.ext.domain;

import java.util.Collections;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 */
public class AlertEventDTO {

    private AlertLevelDTO alertLevelDTO;

    private final String name;

    private final boolean active;

    private final String emailSubject;

    private final String emailBody;

    private final Map<String, String> properties;

    public AlertEventDTO(AlertLevelDTO alertLevelDTO,
                         String name,
                         boolean active,
                         String emailSubject,
                         String emailBody,
                         Map<String, String> properties) {
        this.alertLevelDTO = alertLevelDTO;
        this.name = name;
        this.active = active;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
        this.properties = properties;
    }

    public AlertLevelDTO getAlertLevelDTO() {
        return alertLevelDTO;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setAlertLevelDTO(AlertLevelDTO alertLevelDTO) {
        this.alertLevelDTO = alertLevelDTO;
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

}
