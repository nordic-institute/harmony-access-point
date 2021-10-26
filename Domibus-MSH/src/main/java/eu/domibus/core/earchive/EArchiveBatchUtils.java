package eu.domibus.core.earchive;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
            throw new  DomibusEArchiveException("Could not read batch list from batch:" + eArchiveBatchByBatchId.getBatchId(), e);
        }
    }

    public List<String> getMessageIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getMessageId).collect(Collectors.toList());
    }

    public List<Long> getEntityIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }
}
