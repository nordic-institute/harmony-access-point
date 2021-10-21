package eu.domibus.core.converter;


import eu.domibus.api.earchive.EArchiveBatchDTO;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface EArchiveBatchMapper {


    @WithoutAuditAndEntityId
    EArchiveBatchEntity dtoToEntity(EArchiveBatchDTO value);

    @Mapping(ignore = true, target = "messageStartDate")
    @Mapping(ignore = true, target = "messageEndDate")
    @Mapping(ignore = true, target = "manifestChecksum")
    @Mapping(ignore = true, target = "messages")
    EArchiveBatchDTO entityToDto(EArchiveBatchEntity value);
}
