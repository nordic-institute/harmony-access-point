package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.ext.domain.AlertEventDTO;
import eu.domibus.ext.domain.AlertLevelDTO;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

/**
 * Mapper to generate Monitoring Service abstract class conversion methods
 * @author Soumya Chandran
 * @since 4.2
 */
@Mapper(componentModel = "spring")
@DecoratedWith(AlertMapperDecorator.class)
public interface AlertMapper {

    AlertEventDTO alertEventToAlertEventDTO(AlertEvent alertEvent);

    AlertEvent alertEventDTOToAlertEvent(AlertEventDTO alertEventDTO);

    AlertLevelDTO alertLevelToAlertLevelDTO(AlertLevel alertLevel);

    AlertLevel alertLevelDTOToAlertLevel(AlertLevelDTO alertLevelDTO);

}
