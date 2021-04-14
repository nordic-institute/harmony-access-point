package eu.domibus.core.converter;

import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.persist.Alert;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(uses = {EventMapper.class}, componentModel = "spring")
public interface AlertCoreMapper {

    @WithoutMetadata
    Alert alertServiceToAlertPersist(eu.domibus.core.alerts.model.service.Alert alert);

    eu.domibus.core.alerts.model.service.Alert alertPersistToAlertService(Alert alert);

    List<eu.domibus.core.alerts.model.service.Alert> alertPersistListToAlertServiceList(List<Alert> alertList);

}
