package eu.domibus.core.converter;


import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface EArchiveBatchMapper {


    @Mapping(ignore = true, target = "messageStartId")
    @Mapping(ignore = true, target = "messageEndId")
    @Mapping(ignore = true, target = "manifestChecksum")
    @Mapping(ignore = true, target = "messages")
    EArchiveBatchRequestDTO eArchiveBatchRequestEntityToDto(EArchiveBatchEntity value);


}
