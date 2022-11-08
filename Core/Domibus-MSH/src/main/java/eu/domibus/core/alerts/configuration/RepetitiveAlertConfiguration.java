package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.configuration.FrequencyAlertConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.1
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
