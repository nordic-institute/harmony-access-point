package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class AlertModuleConfigurationBase implements AlertModuleConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertModuleConfigurationBase.class);

    AlertType alertType;
    private boolean active;
    private AlertLevel alertLevel;
    private String mailSubject;

    private AlertModuleConfigurationBase(AlertType alertType, boolean isActive) {
        this.alertType = alertType;
        this.active = isActive;
    }

    public AlertModuleConfigurationBase(AlertType alertType) {
        this(alertType, false);
    }

    public AlertModuleConfigurationBase(AlertType alertType, AlertLevel alertLevel, String emailSubject) {
        this(alertType, true);

        this.alertLevel = alertLevel;
        this.mailSubject = emailSubject;
    }

    public AlertModuleConfigurationBase(AlertType alertType, String emailSubject) {
        this(alertType, null, emailSubject);
    }

    @Override
    public String getMailSubject() {
        return mailSubject;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public AlertLevel getAlertLevel(Event event) {
        AlertType alertTypeFromEvent =  AlertType.getByEventType(event.getType());
        if (this.alertType != alertTypeFromEvent) {
            LOG.error("Invalid alert type: [{}] for this strategy, expected: [{}]", alertTypeFromEvent, this.alertType);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return alertLevel;
    }

}
