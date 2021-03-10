package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper to generate PMode abstract class conversion methods
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface MessageExtMapper {

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);

    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);

    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

    List<MessageAcknowledgementDTO> messageAcknowledgementToMessageAcknowledgementDTO(List<MessageAcknowledgement> messageAcknowledgement);

    List<MessageAttemptDTO> messageAttemptToMessageAttemptDTO(List<MessageAttempt> attemptsHistory);
}
