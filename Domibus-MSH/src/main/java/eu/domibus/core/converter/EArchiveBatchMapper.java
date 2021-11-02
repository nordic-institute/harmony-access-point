package eu.domibus.core.converter;


import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.core.earchive.EArchiveBatchBaseEntity;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public abstract class EArchiveBatchMapper {

    @Autowired
    protected EArchiveBatchUtils archiveBatchUtils;

    @Mapping(ignore = true, target = "manifestChecksum")
    @Mapping(ignore = true, target = "version")
    @Mapping(source = "eArchiveBatchStatus", target = "status")
    @Mapping(source = "errorMessage", target = "errorDescription")
    @Mapping(source = "dateRequested", target = "timestamp")
    @Mapping(source = "lastPkUserMessage", target = "messageEndId")
    @Mapping(source = "firstPkUserMessage", target = "messageStartId")
    @Mapping(source = "batchBaseEntity", target = "messages", qualifiedByName = "userMessageListToMessageIdList")
    public abstract EArchiveBatchRequestDTO eArchiveBatchRequestEntityToDto(EArchiveBatchBaseEntity batchBaseEntity);

    @Named("userMessageListToMessageIdList")
    public List<String> userMessageListToMessageIdList(EArchiveBatchBaseEntity entity) {
        if (entity instanceof EArchiveBatchEntity) {
            ListUserMessageDto listUserMessageDto = archiveBatchUtils.getUserMessageDtoFromJson((EArchiveBatchEntity) entity);
            return archiveBatchUtils.getMessageIds(listUserMessageDto.getUserMessageDtos());
        }
        // return modifiable list
        return Arrays.asList();
    }


}
