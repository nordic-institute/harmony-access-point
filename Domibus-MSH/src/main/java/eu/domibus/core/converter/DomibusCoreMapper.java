package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.plugin.routing.BackendFilterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface DomibusCoreMapper {
    @Mappings({
//            @Mapping(target="employeeId", source="entity.id"),
//            @Mapping(target="employeeName", source="entity.name")
    })
    Process processAPIToProcess(eu.domibus.api.process.Process process);

    @Mappings({
//            @Mapping(target="id", source="dto.employeeId"),
//            @Mapping(target="name", source="dto.employeeName")
    })
    eu.domibus.api.process.Process processToProcessAPI(Process process);

    DomainSpi domainToDomainSpi(Domain domain);
    Domain domainSpiToDomain(DomainSpi domainSpi);

    BackendFilterEntity backendFilterToBackendFilterEntity(BackendFilter backendFilter);
    BackendFilter backendFilterEntityToBackendFilter(BackendFilterEntity backendFilterEntity);

    MessageAttemptEntity messageAttemptToMessageAttemptEntity(MessageAttempt messageAttempt);
    MessageAttempt messageAttemptEntityToMessageAttempt(MessageAttemptEntity messageAttemptEntity);
}
