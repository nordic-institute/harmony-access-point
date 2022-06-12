package eu.domibus.core.converter;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pki.TruststoreInfo;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.crypto.TruststoreEntity;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PasswordEncryptionResultDTO;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.LoggingLevelRO;
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
    @Mapping(ignore = true, target = "inputPattern")
    @Mapping(ignore = true, target = "tooltip")
    RoutingCriteriaEntity routingCriteriaToRoutingCriteriaEntity(RoutingCriteria routingCriteria);

    RoutingCriteria routingCriteriaEntityToRoutingCriteria(RoutingCriteriaEntity routingCriteriaEntity);

    List<LoggingLevelRO> loggingEntryListToLoggingLevelROList(List<LoggingEntry> loggingEntryList);

    LoggingLevelRO loggingEntryToLoggingLevelRO(LoggingEntry loggingEntryList);

    LoggingEntry loggingLevelROToLoggingEntry(LoggingLevelRO loggingEntryList);

    List<PModeResponseRO> pModeArchiveInfoListToPModeResponseROList(List<PModeArchiveInfo> pModeArchiveInfoList);

    TruststoreInfo truststoreEntityToTruststoreInfo(TruststoreEntity entity);
}
