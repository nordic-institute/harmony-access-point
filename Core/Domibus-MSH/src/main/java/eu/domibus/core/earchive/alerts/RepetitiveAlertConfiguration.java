package eu.domibus.core.earchive.alerts;

import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class RepetitiveAlertConfiguration extends FrequencyAlertConfiguration {

    private Integer delay;

    public RepetitiveAlertConfiguration(AlertType alertType) {
        super(alertType);
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

}
