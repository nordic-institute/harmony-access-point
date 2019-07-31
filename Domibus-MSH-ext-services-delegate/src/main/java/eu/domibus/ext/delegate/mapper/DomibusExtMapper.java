package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.domain.*;
import org.mapstruct.Mapper;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(componentModel = "spring")
public interface DomibusExtMapper {
    DomainDTO domainToDomainDTO(Domain domain);

    Domain domainDTOToDomain(DomainDTO domain);

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);

    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);

    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

    JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage);

    JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO);

    UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO);

    UserMessageDTO userMessageToUserMessageDTO(UserMessage userMessage);

    PModeArchiveInfoDTO pModeArchiveInfoToPModeArchiveInfoDto(PModeArchiveInfo pModeArchiveInfo);

    PasswordEncryptionResultDTO passwordEncryptionResultToPasswordEncryptionResultDTO(PasswordEncryptionResult passwordEncryptionResult);
}
