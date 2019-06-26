package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.User;
import eu.domibus.clustering.CommandEntity;
import eu.domibus.common.model.audit.Audit;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.alerts.model.persist.DateEventProperty;
import eu.domibus.core.alerts.model.persist.Event;
import eu.domibus.core.alerts.model.persist.StringEventProperty;
import eu.domibus.core.alerts.model.service.DatePropertyValue;
import eu.domibus.core.alerts.model.service.StringPropertyValue;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.ebms3.common.model.PullRequest;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.plugin.routing.BackendFilterEntity;
import eu.domibus.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.web.rest.ro.*;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import org.mapstruct.*;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(componentModel = "spring")
@DecoratedWith(AbstractPropertyValueDecorator.class)
public interface DomibusCoreMapper {

    Process processAPIToProcess(eu.domibus.api.process.Process process);

    eu.domibus.api.process.Process processToProcessAPI(Process process);

    DomainSpi domainToDomainSpi(Domain domain);

    Domain domainSpiToDomain(DomainSpi domainSpi);

    MessageFilterRO backendFilterToMessageFilterRO(BackendFilter backendFilter);

    BackendFilter messageFilterROToBackendFilter(MessageFilterRO backendFilterEntity);

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

    PluginUserRO authenticationEntityToPluginUserRO(AuthenticationEntity authenticationEntity);

    AuthenticationEntity pluginUserROToAuthenticationEntity(PluginUserRO pluginUserRO);

    @Mapping(target = "properties", ignore = true)
    Event eventServiceToEventPersist(eu.domibus.core.alerts.model.service.Event event);

    @Mapping(target = "properties", ignore = true)
    eu.domibus.core.alerts.model.service.Event eventPersistToEventService(Event event);

    @Mapping(source = "value", target = "stringValue")
    StringEventProperty stringPropertyValueToStringEventProperty(StringPropertyValue propertyValue);

    @InheritInverseConfiguration
    StringPropertyValue stringEventPropertyToStringPropertyValue(StringEventProperty eventProperty);

    @Mapping(source = "value", target = "dateValue")
    DateEventProperty datePropertyValueToDateEventProperty(DatePropertyValue propertyValue);

    @InheritInverseConfiguration
    DatePropertyValue dateEventPropertyToDatePropertyValue(DateEventProperty eventProperty);

    PModeResponseRO pModeArchiveInfoToPModeResponseRO(PModeArchiveInfo pModeArchiveInfo);

    PModeArchiveInfo pModeResponseROToPModeArchiveInfo(PModeResponseRO pModeResponseRO);

    TrustStoreRO trustStoreEntryToTrustStoreRO(TrustStoreEntry trustStoreEntry);

    TrustStoreEntry trustStoreROToTrustStoreEntry(TrustStoreRO trustStoreRO);

    AuditResponseRo auditLogToAuditResponseRo(AuditLog auditLog);

    AuditLog auditResponseRoToAuditLog(AuditResponseRo auditResponseRo);

    DomainRO domainToDomainRO(Domain domain);

    Domain domainROToDomain(DomainRO domain);

    UserResponseRO userToUserResponseRO(User user);

    User userResponseROToUser(UserResponseRO user);

    eu.domibus.common.model.security.User userApiToUserSecurity(User user);

    User userSecurityToUserApi(eu.domibus.common.model.security.User user);

    Audit auditLogToAudit(AuditLog auditLog);

    AuditLog auditToAuditLog(Audit audit);

    LoggingLevelRO loggingEntryToLoggingLevelRO(LoggingEntry loggingEntry);

    LoggingEntry loggingLevelROToLoggingEntry(LoggingLevelRO loggingLevelRO);

    Party configurationPartyToParty(eu.domibus.common.model.configuration.Party party);

    eu.domibus.common.model.configuration.Party partyToConfigurationParty(Party party);

    eu.domibus.api.process.Process processRoToProcess(ProcessRo processRo);

    ProcessRo processToProcessRo(eu.domibus.api.process.Process process);

    Alert alertServiceToAlertPersist(eu.domibus.core.alerts.model.service.Alert alert);

    eu.domibus.core.alerts.model.service.Alert alertPersistToAlertService(Alert alert);

    MessageLogInfo messageLogROToMessageLogInfo(MessageLogRO messageLogRO);

    MessageLogRO messageLogInfoToMessageLogRO(MessageLogInfo messageLogInfo);

    UIMessageDiffEntity uiMessageEntityToUIMessageDiffEntity(UIMessageEntity uiMessageEntity);

    UIMessageEntity uiMessageDiffEntityToUIMessageEntity(UIMessageDiffEntity uiMessageEntity);

    @Mapping(source = "fromId", target = "fromPartyId")
    @Mapping(source = "toId", target = "toPartyId")
    @Mapping(source = "fromScheme", target = "originalSender")
    @Mapping(source = "toScheme", target = "finalRecipient")
    MessageLogRO uiMessageEntityToMessageLogRO(UIMessageEntity uiMessageEntity);

    @InheritInverseConfiguration
    UIMessageEntity messageLogROToUIMessageEntity(MessageLogRO messageLogRO);

    @Mapping(source = "fromId", target = "fromPartyId")
    @Mapping(source = "toId", target = "toPartyId")
    @Mapping(source = "fromScheme", target = "originalSender")
    @Mapping(source = "toScheme", target = "finalRecipient")
    MessageLogInfo uiMessageEntityToMessageLogInfo(UIMessageEntity uiMessageEntity);

    @InheritInverseConfiguration
    UIMessageEntity messageLogInfoToUIMessageEntity(MessageLogInfo messageLogInfo);

    SignalMessageLog uiMessageEntityToSignalMessageLog(UIMessageEntity uiMessageEntity);

    UIMessageEntity signalMessageLogToUIMessageEntity(SignalMessageLog signalMessageLog);

    UserMessageLog uiMessageEntityToUserMessageLog(UIMessageEntity uiMessageEntity);

    UIMessageEntity userMessageLogToUIMessageEntity(UserMessageLog userMessageLog);

    CommandEntity commandToCommandEntity(Command command);

    CommandEntity commandEntityToCommand(CommandEntity commandEntity);

    CertificateEntrySpi certificateEntryToCertificateEntrySpi(CertificateEntry certificateEntry);

    CertificateEntry certificateEntrySpiToCertificateEntry(CertificateEntrySpi certificateEntrySpi);

    ErrorLogEntry errorLogROToErrorLogEntry(ErrorLogRO errorLogRO);

    ErrorLogRO errorLogEntryToErrorLogRO(ErrorLogEntry errorLogEntry);

    PartyResponseRo partyToPartyResponseRo(Party party);

    Party partyResponseRoToParty(PartyResponseRo partyResponseRo);

    eu.domibus.api.usermessage.domain.UserMessage userMessageToUserMessageApi(UserMessage userMessage);

    UserMessage userMessageApiToUserMessage(eu.domibus.api.usermessage.domain.UserMessage userMessage);
}
