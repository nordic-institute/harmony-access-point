package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.plugin.routing.BackendFilterEntity;
import eu.domibus.plugin.routing.RoutingCriteriaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomibusCoreMapper {

    Process processAPIToProcess(eu.domibus.api.process.Process process);
    eu.domibus.api.process.Process processToProcessAPI(Process process);

    DomainSpi domainToDomainSpi(Domain domain);
    Domain domainSpiToDomain(DomainSpi domainSpi);

    BackendFilterEntity backendFilterToBackendFilterEntity(BackendFilter backendFilter);
    BackendFilter backendFilterEntityToBackendFilter(BackendFilterEntity backendFilterEntity);

    RoutingCriteria routingCriteriaEntityToRoutingCriteria(RoutingCriteriaEntity routingCriteriaEntity);
    RoutingCriteriaEntity routingCriteriaToRoutingCriteriaEntity(RoutingCriteria routingCriteria);

    MessageAttemptEntity messageAttemptToMessageAttemptEntity(MessageAttempt messageAttempt);
    MessageAttempt messageAttemptEntityToMessageAttempt(MessageAttemptEntity messageAttemptEntity);

    UserMessageDTO userMessageToUserMessageDTO(UserMessage userMessage);
    UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO);

    PullRequestDTO pullRequestToPullRequestDTO(PullRequest pullRequest);
    PullRequest pullRequestDTOToPullRequest(PullRequestDTO pullRequestDTO);


}
