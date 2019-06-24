package eu.domibus.core.converter;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.process.Process;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.routing.BackendFilterEntity;
import eu.domibus.plugin.routing.RoutingCriteriaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DomainCoreDefaultConverter implements DomainCoreConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCoreDefaultConverter.class);

    @Autowired
    DomibusCoreMapper domibusCoreMapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        if(typeOfT == Process.class) {
            return (T)domibusCoreMapper.processToProcessAPI((eu.domibus.common.model.configuration.Process)source);
        }

        if(typeOfT == eu.domibus.common.model.configuration.Process.class) {
            return (T)domibusCoreMapper.processAPIToProcess((Process)source);
        }

        if(typeOfT == Domain.class) {
            return (T)domibusCoreMapper.domainSpiToDomain((DomainSpi) source);
        }
        if(typeOfT == DomainSpi.class) {
            return (T)domibusCoreMapper.domainToDomainSpi((Domain)source);
        }

        if(typeOfT == BackendFilter.class) {
            return (T)domibusCoreMapper.backendFilterEntityToBackendFilter((BackendFilterEntity) source);
        }
        if(typeOfT == BackendFilterEntity.class) {
            return (T)domibusCoreMapper.backendFilterToBackendFilterEntity((BackendFilter) source);
        }

        if(typeOfT == RoutingCriteria.class) {
            return (T)domibusCoreMapper.routingCriteriaEntityToRoutingCriteria((RoutingCriteriaEntity) source);
        }
        if(typeOfT == RoutingCriteriaEntity.class) {
            return (T)domibusCoreMapper.routingCriteriaToRoutingCriteriaEntity((RoutingCriteria) source);
        }

        if(typeOfT == MessageAttempt.class) {
            return (T)domibusCoreMapper.messageAttemptEntityToMessageAttempt((MessageAttemptEntity) source);
        }
        if(typeOfT == MessageAttemptEntity.class) {
            return (T)domibusCoreMapper.messageAttemptToMessageAttemptEntity((MessageAttempt) source);
        }


        if(typeOfT == UserMessage.class) {
            return (T)domibusCoreMapper.userMessageDTOToUserMessage((UserMessageDTO) source);
        }
        if(typeOfT == UserMessageDTO.class) {
            return (T)domibusCoreMapper.userMessageToUserMessageDTO((UserMessage) source);
        }

        if(typeOfT == PullRequest.class) {
            return (T)domibusCoreMapper.pullRequestDTOToPullRequest((PullRequestDTO) source);
        }
        if(typeOfT == PullRequestDTO.class) {
            return (T)domibusCoreMapper.pullRequestToPullRequestDTO((PullRequest) source);
        }

        LOG.warn("Type not converted: T=[{}] U=[{}]", typeOfT, source.getClass());
        return null;
    }

    @Override
    public <T, U> List<T> convert(List<U> sourceList, final Class<T> typeOfT) {
        if (sourceList == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (U sourceObject : sourceList) {
            result.add(convert(sourceObject, typeOfT));

        }
        return result;
    }

}
