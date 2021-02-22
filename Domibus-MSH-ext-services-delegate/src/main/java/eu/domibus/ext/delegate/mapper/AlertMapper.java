package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.alerts.AlertEvent;
import eu.domibus.ext.domain.AlertEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper to generate Monitoring Service abstract class conversion methods
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface AlertMapper {

    @Mapping(source = "alertLevel", target = "alertLevelDTO")
    AlertEventDTO alertEventToAlertEventDTO(AlertEvent alertEvent);

    @Mapping(source = "alertLevelDTO", target = "alertLevel")
    AlertEvent alertEventDTOToAlertEvent(AlertEventDTO alertEventDTO);

}
