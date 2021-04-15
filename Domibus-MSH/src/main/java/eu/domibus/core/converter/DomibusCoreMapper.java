package eu.domibus.core.converter;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.clustering.CommandEntity;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.core.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.PModeResponseRO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(componentModel = "spring")
public interface DomibusCoreMapper {

    @Mapping(target = "current", ignore = true)
    PModeResponseRO pModeArchiveInfoToPModeResponseRO(PModeArchiveInfo pModeArchiveInfo);

    PModeArchiveInfo pModeResponseROToPModeArchiveInfo(PModeResponseRO pModeResponseRO);

    PasswordEncryptionResult passwordEncryptionResultDTOToPasswordEncryptionResult(PasswordEncryptionResultDTO passwordEncryptionResult);

    PasswordEncryptionResultDTO passwordEncryptionResultToPasswordEncryptionResultDTO(PasswordEncryptionResult passwordEncryptionResult);

    DomainSpi domainToDomainSpi(Domain domain);

    Domain domainSpiToDomain(DomainSpi domainSpi);

    DomainDTO domainToDomainDTO(Domain domain);

    Domain domainDTOToDomain(DomainDTO domain);

    DomainRO domainToDomainRO(Domain domain);

    Domain domainROToDomain(DomainRO domain);

    List<DomainRO> domainListToDomainROList(List<Domain> domainList);

    @WithoutAudit
    BackendFilterEntity backendFilterToBackendFilterEntity(BackendFilter backendFilter);

    @WithoutAudit
    @Mapping(ignore = true, target = "inputPattern")
    @Mapping(ignore = true, target = "tooltip")
    RoutingCriteriaEntity routingCriteriaToRoutingCriteriaEntity(RoutingCriteria routingCriteria);

    RoutingCriteria routingCriteriaEntityToRoutingCriteria(RoutingCriteriaEntity routingCriteriaEntity);

    List<BackendFilterEntity> backendFilterListToBackendFilterEntityList(List<BackendFilter> backendFilterList);

    List<BackendFilter> backendFilterEntityListToBackendFilterList(List<BackendFilterEntity> backendFilterEntityList);

    List<BackendFilter> messageFilterROListToBackendFilterList(List<MessageFilterRO> messageFilterROList);

    @Mapping(ignore = true, target = "active")
    BackendFilter backendFilterEntityToBackendFilter(BackendFilterEntity backendFilterEntity);

    @Mapping(ignore = true, target = "active")
    BackendFilter messageFilterROToBackendFilter(MessageFilterRO messageFilterRO);

    List<LoggingLevelRO> loggingEntryListToLoggingLevelROList(List<LoggingEntry> loggingEntryList);

    LoggingLevelRO loggingEntryToLoggingLevelRO(LoggingEntry loggingEntryList);

    LoggingEntry loggingLevelROToLoggingEntry(LoggingLevelRO loggingEntryList);

    @Mapping(ignore = true, target = "persisted")
    MessageFilterRO backendFilterToMessageFilterRO(BackendFilter backendFilter);

    List<MessageFilterRO> backendFilterListToMessageFilterROList(List<BackendFilter> backendFilterList);

    List<Command> commandEntityListToCommandList(List<CommandEntity> commandEntityList);

    Command commandEntityToCommand(CommandEntity commandEntity);

    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    CommandEntity commandToCommandEntity(Command commandEntity);

    List<PModeResponseRO> pModeArchiveInfoListToPModeResponseROList(List<PModeArchiveInfo> pModeArchiveInfoList);


}
