package eu.domibus.core.converter;


import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public abstract class EArchiveBatchMapper {

    @Autowired
    protected EArchiveBatchUtils archiveBatchUtils;

    @Mapping(ignore = true, target = "version")
    @Mapping(source = "EArchiveBatchStatus", target = "status")
    @Mapping(source = "errorMessage", target = "errorDescription")
    @Mapping(source = "dateRequested", target = "timestamp")
    @Mapping(source = "lastPkUserMessage", target = "messageEndDate")
    @Mapping(source = "firstPkUserMessage", target = "messageStartDate")
    @Mapping(source = "batchBaseEntity", target = "messages", qualifiedByName = "userMessageListToMessageIdList")
    public abstract EArchiveBatchRequestDTO eArchiveBatchRequestEntityToDto(EArchiveBatchEntity batchBaseEntity);

    @Named("userMessageListToMessageIdList")
    public List<String> userMessageListToMessageIdList(EArchiveBatchEntity entity) {
        return archiveBatchUtils.getMessageIds(entity.geteArchiveBatchUserMessages());
    }


}
