package eu.domibus.core.earchive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX_INCREMENT_NUMBER;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Component
public class EArchiveBatchUtils {

    private final ObjectMapper objectMapper;

    public EArchiveBatchUtils(@Qualifier("domibusJsonMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ListUserMessageDto getUserMessageDtoFromJson(EArchiveBatchEntity eArchiveBatchByBatchId) {
        try {
            return objectMapper.readValue(new String(eArchiveBatchByBatchId.getMessageIdsJson(), StandardCharsets.UTF_8), ListUserMessageDto.class);
        } catch (IOException e) {
            throw new DomibusEArchiveException("Could not read batch list from batch:" + eArchiveBatchByBatchId.getBatchId(), e);
        }
    }

    public List<String> getMessageIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getMessageId).collect(Collectors.toList());
    }

    public List<Long> getEntityIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }

    public String getRawJson(ListUserMessageDto userMessageToBeArchived) {
        try {
            return objectMapper.writeValueAsString(userMessageToBeArchived);
        } catch (JsonProcessingException e) {
            throw new DomibusEArchiveException("Could not parse the list of userMessages", e);
        }
    }

    public Long extractDateFromPKUserMessageId(Long pkUserMessage) {
        if (pkUserMessage == null) {
            return null;
        }
        return pkUserMessage / (MAX_INCREMENT_NUMBER + 1);
    }

    Long dateToPKUserMessageId(Long pkUserMessageDate) {
        return pkUserMessageDate == null ? null : pkUserMessageDate * (MAX_INCREMENT_NUMBER + 1);
    }
}
