package eu.domibus.core.converter;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface CommandCoreMapper {

    List<Command> commandEntityListToCommandList(List<CommandEntity> commandEntityList);

    Command commandEntityToCommand(CommandEntity commandEntity);

    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    CommandEntity commandToCommandEntity(Command commandEntity);
}
