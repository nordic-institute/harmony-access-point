package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class RepetitiveAlertConfiguration extends DefaultAlertConfiguration {

    private Integer eventDelay;

    private Integer eventFrequency;

    public RepetitiveAlertConfiguration(AlertType alertType) {
        super(alertType);
    }

    public RepetitiveAlertConfiguration(AlertType alertType, AlertLevel eventAlertLevel, String eventMailSubject,
                                        Integer eventDelay, Integer eventFrequency) {
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

    public void setEventDelay(Integer eventDelay) {
        this.eventDelay = eventDelay;
    }

    public void setEventFrequency(Integer eventFrequency) {
        this.eventFrequency = eventFrequency;
    }

}
