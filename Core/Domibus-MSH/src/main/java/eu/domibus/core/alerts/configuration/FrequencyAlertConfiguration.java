package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class FrequencyAlertConfiguration extends AlertModuleConfigurationBase {

    private Integer frequency;

    public FrequencyAlertConfiguration(AlertType alertType) {
        super(alertType);
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

}
