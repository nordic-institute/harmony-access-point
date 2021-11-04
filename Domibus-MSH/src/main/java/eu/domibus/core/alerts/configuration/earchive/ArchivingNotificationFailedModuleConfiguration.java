package eu.domibus.core.alerts.configuration.earchive;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class ArchivingNotificationFailedModuleConfiguration extends AlertModuleConfigurationBase {

    public ArchivingNotificationFailedModuleConfiguration() {
        super(AlertType.ARCHIVING_NOTIFICATION_FAILED);
    }

    public ArchivingNotificationFailedModuleConfiguration(AlertLevel alertLevel, String alertMailSubject) {
        super(AlertType.ARCHIVING_NOTIFICATION_FAILED, alertLevel, alertMailSubject);
    }

}
