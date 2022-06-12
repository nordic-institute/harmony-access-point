package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author François Gautier
 * @since 5.0
 */
public class ArchivingStartDateStoppedModuleConfiguration extends AlertModuleConfigurationBase {

    public ArchivingStartDateStoppedModuleConfiguration() {
        super(AlertType.ARCHIVING_START_DATE_STOPPED);
    }

    public ArchivingStartDateStoppedModuleConfiguration(AlertLevel alertLevel, String alertMailSubject) {
        super(AlertType.ARCHIVING_START_DATE_STOPPED, alertLevel, alertMailSubject);
    }
}
