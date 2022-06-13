package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author François Gautier
 * @since 5.0
 */
public class ArchivingMessagesNonFinalModuleConfiguration  extends AlertModuleConfigurationBase {

    public ArchivingMessagesNonFinalModuleConfiguration() {
        super(AlertType.ARCHIVING_MESSAGES_NON_FINAL);
    }

    public ArchivingMessagesNonFinalModuleConfiguration(AlertLevel alertLevel, String alertMailSubject) {
        super(AlertType.ARCHIVING_MESSAGES_NON_FINAL, alertLevel, alertMailSubject);
    }
}
