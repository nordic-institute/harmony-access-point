package eu.domibus.core.alerts.configuration.partitions;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author idragusa
 * @since 5.0
 */
public class PartitionsModuleConfiguration extends AlertModuleConfigurationBase {

    private Integer eventFrequency;

    public PartitionsModuleConfiguration() {
        super(AlertType.PARTITION_CHECK);
    }

    public PartitionsModuleConfiguration(AlertLevel alertLevel, String emailSubject, Integer eventFrequency) {
        super(AlertType.PARTITION_CHECK, alertLevel, emailSubject);
        this.eventFrequency = eventFrequency;
    }

    public Integer getEventFrequency() {
        return eventFrequency;
    }
}
