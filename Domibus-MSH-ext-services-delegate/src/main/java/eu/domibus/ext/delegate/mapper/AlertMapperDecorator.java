package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.ext.domain.AlertEventDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Monitoring Mapper Decorator is for the abstract class and override the methods of the monitoring mapper which it decorates.
 *
 * @author François Gautier
 * @since 5.0
 */
public abstract class AlertMapperDecorator implements AlertMapper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AlertMapperDecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected AlertMapper delegate;

    public AlertMapperDecorator() {
    }

    @Override
    public AlertEventDTO alertEventToAlertEventDTO(AlertEvent alertEvent) {
        AlertEventDTO alertEventDTO = delegate.alertEventToAlertEventDTO(alertEvent);
        alertEventDTO.setAlertLevelDTO(delegate.alertLevelToAlertLevelDTO(alertEvent.getAlertLevel()));
        return alertEventDTO;
    }

    @Override
    public AlertEvent alertEventDTOToAlertEvent(AlertEventDTO alertEventDTO) {
        AlertEvent alertEvent = delegate.alertEventDTOToAlertEvent(alertEventDTO);
        alertEvent.setAlertLevel(delegate.alertLevelDTOToAlertLevel(alertEventDTO.getAlertLevelDTO()));
        return alertEvent;
    }
}



