package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.converter.ConverterException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.process.Process;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.User;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.audit.model.mapper.AuditMapper;
import eu.domibus.core.clustering.CommandEntity;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.core.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.ebms3.common.model.PartProperties;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.PartPropertiesDTO;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu, idragusa
 * @since 3.3
 */
@Component
public class DomainCoreDefaultConverter implements DomainCoreConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCoreDefaultConverter.class);

    @Autowired
    DomibusCoreMapper domibusCoreMapper;

    @Autowired
    EventMapper eventMapper;

    @Autowired
    AuditMapper auditMapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        if (typeOfT == Process.class && source.getClass() == eu.domibus.common.model.configuration.Process.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.processToProcessAPI((eu.domibus.common.model.configuration.Process) source);
        }

        if (typeOfT == eu.domibus.common.model.configuration.Process.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.processAPIToProcess((Process) source);
        }

        if (typeOfT == Domain.class && source.getClass() == DomainSpi.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.domainSpiToDomain((DomainSpi) source);
        }
        if (typeOfT == DomainSpi.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.domainToDomainSpi((Domain) source);
        }

        if (typeOfT == BackendFilter.class && source.getClass() == BackendFilterEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.backendFilterEntityToBackendFilter((BackendFilterEntity) source);
        }
        if (typeOfT == BackendFilterEntity.class && source.getClass() == BackendFilter.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.backendFilterToBackendFilterEntity((BackendFilter) source);
        }

        if (typeOfT == BackendFilter.class && source.getClass() == MessageFilterRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageFilterROToBackendFilter((MessageFilterRO) source);
        }
        if (typeOfT == MessageFilterRO.class && source.getClass() == BackendFilter.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.backendFilterToMessageFilterRO((BackendFilter) source);
        }

        if (typeOfT == RoutingCriteria.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.routingCriteriaEntityToRoutingCriteria((RoutingCriteriaEntity) source);
        }
        if (typeOfT == RoutingCriteriaEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.routingCriteriaToRoutingCriteriaEntity((RoutingCriteria) source);
        }

        if (typeOfT == MessageAttempt.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageAttemptEntityToMessageAttempt((MessageAttemptEntity) source);
        }
        if (typeOfT == MessageAttemptEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageAttemptToMessageAttemptEntity((MessageAttempt) source);
        }

        if (typeOfT == UserMessage.class && source.getClass() == UserMessageDTO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userMessageDTOToUserMessage((UserMessageDTO) source);
        }
        if (typeOfT == UserMessageDTO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userMessageToUserMessageDTO((UserMessage) source);
        }

        if (typeOfT == PullRequest.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.pullRequestDTOToPullRequest((PullRequestDTO) source);
        }
        if (typeOfT == PullRequestDTO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.pullRequestToPullRequestDTO((PullRequest) source);
        }

        if (typeOfT == PluginUserRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.authenticationEntityToPluginUserRO((AuthenticationEntity) source);
        }
        if (typeOfT == AuthenticationEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.pluginUserROToAuthenticationEntity((PluginUserRO) source);
        }

        if (typeOfT == eu.domibus.core.alerts.model.service.Event.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) eventMapper.eventPersistToEventService((Event) source);
        }
        if (typeOfT == Event.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) eventMapper.eventServiceToEventPersist((eu.domibus.core.alerts.model.service.Event) source);
        }

        if (typeOfT == PModeResponseRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.pModeArchiveInfoToPModeResponseRO((PModeArchiveInfo) source);
        }
        if (typeOfT == PModeArchiveInfo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.pModeResponseROToPModeArchiveInfo((PModeResponseRO) source);
        }

        if (typeOfT == TrustStoreRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.trustStoreEntryToTrustStoreRO((TrustStoreEntry) source);
        }
        if (typeOfT == TrustStoreEntry.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.trustStoreROToTrustStoreEntry((TrustStoreRO) source);
        }

        if (typeOfT == AuditResponseRo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.auditLogToAuditResponseRo((AuditLog) source);
        }
        if (typeOfT == AuditLog.class && source.getClass() == AuditResponseRo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.auditResponseRoToAuditLog((AuditResponseRo) source);
        }

        if (typeOfT == DomainRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.domainToDomainRO((Domain) source);
        }
        if (typeOfT == Domain.class && source.getClass() == DomainRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.domainROToDomain((DomainRO) source);
        }

        if (typeOfT == UserResponseRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userToUserResponseRO((User) source);
        }
        if (typeOfT == User.class && source.getClass() == UserResponseRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userResponseROToUser((UserResponseRO) source);
        }

        if (typeOfT == eu.domibus.core.user.ui.User.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userApiToUserSecurity((User) source);
        }
        if (typeOfT == User.class && source.getClass() == eu.domibus.core.user.ui.User.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userSecurityToUserApi((eu.domibus.core.user.ui.User) source);
        }

        if (typeOfT == AuditLog.class && source.getClass() == Audit.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.auditToAuditLog((Audit) source);
        }
        if (typeOfT == Audit.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) auditMapper.auditLogToAudit((AuditLog) source);
        }

        if (typeOfT == LoggingLevelRO.class && source.getClass() == LoggingEntry.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.loggingEntryToLoggingLevelRO((LoggingEntry) source);
        }
        if (typeOfT == LoggingEntry.class && source.getClass() == LoggingLevelRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.loggingLevelROToLoggingEntry((LoggingLevelRO) source);
        }

        if (typeOfT == Party.class && source.getClass() == eu.domibus.common.model.configuration.Party.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.configurationPartyToParty((eu.domibus.common.model.configuration.Party) source);
        }
        if (typeOfT == eu.domibus.common.model.configuration.Party.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.partyToConfigurationParty((Party) source);
        }

        if (typeOfT == ProcessRo.class && source.getClass() == Process.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.processToProcessRo((Process) source);
        }
        if (typeOfT == Process.class && source.getClass() == ProcessRo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.processRoToProcess((ProcessRo) source);
        }

        if (typeOfT == Alert.class && source.getClass() == eu.domibus.core.alerts.model.service.Alert.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.alertServiceToAlertPersist((eu.domibus.core.alerts.model.service.Alert) source);
        }
        if (typeOfT == eu.domibus.core.alerts.model.service.Alert.class && source.getClass() == Alert.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.alertPersistToAlertService((Alert) source);
        }

        if (typeOfT == MessageLogRO.class && source.getClass() == MessageLogInfo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageLogInfoToMessageLogRO((MessageLogInfo) source);
        }
        if (typeOfT == MessageLogInfo.class && source.getClass() == MessageLogRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageLogROToMessageLogInfo((MessageLogRO) source);
        }

        if (typeOfT == UIMessageEntity.class && source.getClass() == UIMessageDiffEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageDiffEntityToUIMessageEntity((UIMessageDiffEntity) source);
        }
        if (typeOfT == UIMessageDiffEntity.class && source.getClass() == UIMessageEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageEntityToUIMessageDiffEntity((UIMessageEntity) source);
        }

        if (typeOfT == MessageLogRO.class && source.getClass() == UIMessageEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageEntityToMessageLogRO((UIMessageEntity) source);
        }
        if (typeOfT == UIMessageEntity.class && source.getClass() == MessageLogRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageLogROToUIMessageEntity((MessageLogRO) source);
        }

        if (typeOfT == MessageLogInfo.class && source.getClass() == UIMessageEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageEntityToMessageLogInfo((UIMessageEntity) source);
        }
        if (typeOfT == UIMessageEntity.class && source.getClass() == MessageLogInfo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.messageLogInfoToUIMessageEntity((MessageLogInfo) source);
        }

        if (typeOfT == SignalMessageLog.class && source.getClass() == UIMessageEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageEntityToSignalMessageLog((UIMessageEntity) source);
        }
        if (typeOfT == UIMessageEntity.class && source.getClass() == SignalMessageLog.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.signalMessageLogToUIMessageEntity((SignalMessageLog) source);
        }

        if (typeOfT == UserMessageLog.class && source.getClass() == UIMessageEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.uiMessageEntityToUserMessageLog((UIMessageEntity) source);
        }
        if (typeOfT == UIMessageEntity.class && source.getClass() == UserMessageLog.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userMessageLogToUIMessageEntity((UserMessageLog) source);
        }

        if (typeOfT == Command.class && source.getClass() == CommandEntity.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.commandEntityToCommand((CommandEntity) source);
        }
        if (typeOfT == CommandEntity.class && source.getClass() == Command.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.commandToCommandEntity((Command) source);
        }

        if (typeOfT == CertificateEntry.class && source.getClass() == CertificateEntrySpi.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.certificateEntrySpiToCertificateEntry((CertificateEntrySpi) source);
        }
        if (typeOfT == CertificateEntrySpi.class && source.getClass() == CertificateEntry.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.certificateEntryToCertificateEntrySpi((CertificateEntry) source);
        }

        if (typeOfT == ErrorLogRO.class && source.getClass() == ErrorLogEntry.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.errorLogEntryToErrorLogRO((ErrorLogEntry) source);
        }
        if (typeOfT == ErrorLogEntry.class && source.getClass() == ErrorLogRO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.errorLogROToErrorLogEntry((ErrorLogRO) source);
        }

        if (typeOfT == PartyResponseRo.class && source.getClass() == Party.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.partyToPartyResponseRo((Party) source);
        }
        if (typeOfT == Party.class && source.getClass() == PartyResponseRo.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.partyResponseRoToParty((PartyResponseRo) source);
        }

        if (typeOfT == UserMessage.class && source.getClass() == eu.domibus.api.usermessage.domain.UserMessage.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userMessageApiToUserMessage((eu.domibus.api.usermessage.domain.UserMessage) source);
        }
        if (typeOfT == eu.domibus.api.usermessage.domain.UserMessage.class && source.getClass() == UserMessage.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.userMessageToUserMessageApi((UserMessage) source);
        }

        if (typeOfT == DomibusPropertyRO.class && source.getClass() == DomibusProperty.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.propertyApiToPropertyRO((DomibusProperty) source);
        }

        if (typeOfT == PartProperties.class && source.getClass() == PartPropertiesDTO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.partPropertiesDTOToPartProperties((PartPropertiesDTO) source);
        }
        if (typeOfT == PartPropertiesDTO.class && source.getClass() == PartProperties.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.partPropertiesToPartPropertiesDTO((PartProperties) source);
        }

        if (typeOfT == DomibusPropertyTypeRO.class && source.getClass() == DomibusPropertyMetadata.Type.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.domibusPropertyMetadataTypeTOdomibusPropertyTypeRO((DomibusPropertyMetadata.Type) source);
        }
        if (typeOfT == DomibusPropertyMetadata.class && source.getClass() == DomibusPropertyMetadataDTO.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.propertyMetadataDTOTopropertyMetadata((DomibusPropertyMetadataDTO) source);
        }
        if (typeOfT == DomibusPropertyMetadata.class && source.getClass() == DomibusPropertyMetadata.class) {
            LOG.trace("Type converted: T=[{}] U=[{}]", typeOfT, source.getClass());
            return (T) domibusCoreMapper.propertyMetadataTopropertyMetadata((DomibusPropertyMetadata) source);
        }

        String errorMsg = String.format("Type not converted: T=[%s] U=[%s]", typeOfT, source.getClass());
        LOG.error(errorMsg);
        throw new ConverterException(DomibusCoreErrorCode.DOM_008, errorMsg);
    }

    @Override
    public <T, U> List<T> convert(List<U> sourceList, final Class<T> typeOfT) {
        LOG.trace("Convert list to T=[{} ", typeOfT);
        if (sourceList == null) {
            LOG.trace("SourceList is null for T=[{}", typeOfT);
            return null;
        }
        List<T> result = new ArrayList<>();
        for (U sourceObject : sourceList) {
            result.add(convert(sourceObject, typeOfT));
        }
        return result;
    }

}
