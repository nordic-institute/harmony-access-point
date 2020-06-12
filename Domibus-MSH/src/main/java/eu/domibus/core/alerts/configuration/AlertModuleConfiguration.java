package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.service.Event;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AlertModuleConfiguration {

    String getMailSubject();

    boolean isActive();

    AlertLevel getAlertLevel(final Event event);

}
