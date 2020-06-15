package eu.domibus.core.alerts.configuration.password;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class PasswordExpirationAlertModuleConfiguration extends AlertModuleConfigurationBase {

    private Integer eventDelay;
    private Integer eventFrequency;

    public PasswordExpirationAlertModuleConfiguration(AlertType alertType) {
        super(alertType);
    }

    public PasswordExpirationAlertModuleConfiguration(AlertType alertType, Integer eventDelay, Integer eventFrequency, AlertLevel eventAlertLevel, String eventMailSubject) {
        super(alertType, eventAlertLevel, eventMailSubject);

        this.eventDelay = eventDelay;
        this.eventFrequency = eventFrequency;
    }

    public Integer getEventDelay() {
        return eventDelay;
    }

    public Integer getEventFrequency() {
        return eventFrequency;
    }

}
