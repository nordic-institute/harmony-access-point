package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomibusExtMapper {
    DomainDTO domainToDomainDTO(Domain domain);
    Domain domainDTOToDomain(DomainDTO domain);

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);
    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);
    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

}
