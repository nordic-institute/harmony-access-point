package eu.domibus.core.alerts.configuration.model;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.service.Alert;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AlertModuleConfiguration {

    String getMailSubject();

    boolean isActive();

    AlertLevel getAlertLevel(final Alert alert);

}
