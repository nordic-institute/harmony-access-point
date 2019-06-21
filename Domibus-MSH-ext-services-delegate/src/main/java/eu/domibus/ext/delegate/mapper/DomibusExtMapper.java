package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.domain.MessageAttemptDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DomibusExtMapper {
    @Mappings({
//            @Mapping(target="employeeId", source="entity.id"),
//            @Mapping(target="employeeName", source="entity.name")
    })
    DomainDTO domainToDomainDTO(Domain domain);
    @Mappings({
//            @Mapping(target="id", source="dto.employeeId"),
//            @Mapping(target="name", source="dto.employeeName")
    })
    Domain domainDTOToDomain(DomainDTO domain);

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);
    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);
    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

}
