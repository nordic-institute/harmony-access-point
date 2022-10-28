package eu.domibus.core.earchive.alerts;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DefaultAlertConfiguration extends AlertModuleConfigurationBase {

    public DefaultAlertConfiguration(AlertType alertType) {
        super(alertType);
    }

    public DefaultAlertConfiguration(AlertType alertType, AlertLevel alertLevel, String alertMailSubject) {
        super(alertType, alertLevel, alertMailSubject);
    }

}
