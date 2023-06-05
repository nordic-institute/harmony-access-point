package eu.domibus.core.converter;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.web.rest.ro.MessageFilterRO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface BackendFilterCoreMapper {

    List<BackendFilterEntity> backendFilterListToBackendFilterEntityList(List<BackendFilter> backendFilterList);

    List<BackendFilter> backendFilterEntityListToBackendFilterList(List<BackendFilterEntity> backendFilterEntityList);

    List<BackendFilter> messageFilterROListToBackendFilterList(List<MessageFilterRO> messageFilterROList);

    @Mapping(ignore = true, target = "active")
    BackendFilter backendFilterEntityToBackendFilter(BackendFilterEntity backendFilterEntity);

    @Mapping(ignore = true, target = "active")
    BackendFilter messageFilterROToBackendFilter(MessageFilterRO messageFilterRO);

    @Mapping(ignore = true, target = "persisted")
    MessageFilterRO backendFilterToMessageFilterRO(BackendFilter backendFilter);

    @WithoutAudit
    BackendFilterEntity backendFilterToBackendFilterEntity(BackendFilter backendFilter);

    List<MessageFilterRO> backendFilterListToMessageFilterROList(List<BackendFilter> backendFilterList);

}
