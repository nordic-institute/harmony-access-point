package eu.domibus.core.converter;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.User;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.alerts.model.persist.Alert;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.clustering.CommandEntity;
import eu.domibus.core.crypto.spi.CertificateEntrySpi;
import eu.domibus.core.crypto.spi.DomainSpi;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.logging.LoggingEntry;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.attempt.MessageAttemptEntity;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.core.plugin.routing.BackendFilterEntity;
import eu.domibus.core.plugin.routing.RoutingCriteriaEntity;
import eu.domibus.core.property.DomibusPropertiesFilter;
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.ext.domain.*;
import eu.domibus.web.rest.ro.*;
import org.springframework.stereotype.Service;

import java.util.List;

//TODO remove this when the mapper is fixed
@Service
public class DomibusCoreMapperMock implements DomibusCoreMapper {

    @Override
    public UIMessageDiffEntity uiMessageEntityToUIMessageDiffEntity(UIMessageEntity uiMessageEntity) {
        return null;
    }

    @Override
    public UIMessageEntity uiMessageDiffEntityToUIMessageEntity(UIMessageDiffEntity uiMessageEntity) {
        return null;
    }

    @Override
    public MessageLogRO uiMessageEntityToMessageLogRO(UIMessageEntity uiMessageEntity) {
        return null;
    }

    @Override
    public UIMessageEntity messageLogROToUIMessageEntity(MessageLogRO messageLogRO) {
        return null;
    }

    @Override
    public MessageLogInfo uiMessageEntityToMessageLogInfo(UIMessageEntity uiMessageEntity) {
        return null;
    }

    @Override
    public UIMessageEntity messageLogInfoToUIMessageEntity(MessageLogInfo messageLogInfo) {
        return null;
    }

    @Override
    public SignalMessageLog uiMessageEntityToSignalMessageLog(UIMessageEntity uiMessageEntity) {
        return null;
    }

    @Override
    public UIMessageEntity signalMessageLogToUIMessageEntity(SignalMessageLog signalMessageLog) {
        return null;
    }

    @Override
    public UserMessageLog uiMessageEntityToUserMessageLog(UIMessageEntity uiMessageEntity) {
        return null;
    }

    @Override
    public UIMessageEntity userMessageLogToUIMessageEntity(UserMessageLog userMessageLog) {
        return null;
    }

    @Override
    public PartInfoDTO partInfoToPartInfoDTO(PartInfo partInfo) {
        return null;
    }

    @Override
    public PartInfo partInfoDTOToPartInfo(PartInfoDTO partInfoDTO) {
        return null;
    }

    @Override
    public UserMessageDTO userMessageToUserMessageDTO(eu.domibus.api.model.UserMessage userMessage) {
        return null;
    }

    @Override
    public eu.domibus.api.model.UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO) {
        return null;
    }

    @Override
    public Process processAPIToProcess(eu.domibus.api.process.Process process) {
        return null;
    }

    @Override
    public eu.domibus.api.process.Process processToProcessAPI(Process process) {
        return null;
    }

    @Override
    public DomainSpi domainToDomainSpi(Domain domain) {
        return new DomainSpi(domain.getCode(), domain.getName());
    }

    @Override
    public Domain domainSpiToDomain(DomainSpi domainSpi) {
        return new Domain(domainSpi.getCode(), domainSpi.getName());
    }

    @Override
    public DomainDTO domainToDomainDTO(Domain domain) {
        return new DomainDTO(domain.getCode(), domain.getName());
    }

    @Override
    public Domain domainDTOToDomain(DomainDTO domain) {
        return new Domain(domain.getCode(), domain.getName());
    }

    @Override
    public MessageFilterRO backendFilterToMessageFilterRO(BackendFilter backendFilter) {
        return null;
    }

    @Override
    public BackendFilter messageFilterROToBackendFilter(MessageFilterRO backendFilterEntity) {
        return null;
    }

    @Override
    public BackendFilterEntity backendFilterToBackendFilterEntity(BackendFilter backendFilter) {
        return null;
    }

    @Override
    public BackendFilter backendFilterEntityToBackendFilter(BackendFilterEntity backendFilterEntity) {
        return null;
    }

    @Override
    public RoutingCriteria routingCriteriaEntityToRoutingCriteria(RoutingCriteriaEntity routingCriteriaEntity) {
        return null;
    }

    @Override
    public RoutingCriteriaEntity routingCriteriaToRoutingCriteriaEntity(RoutingCriteria routingCriteria) {
        return null;
    }

    @Override
    public MessageAttemptEntity messageAttemptToMessageAttemptEntity(MessageAttempt messageAttempt) {
        return null;
    }

    @Override
    public MessageAttempt messageAttemptEntityToMessageAttempt(MessageAttemptEntity messageAttemptEntity) {
        return null;
    }

    @Override
    public PropertyDTO propertyToPropertyDTO(Property property) {
        return null;
    }

    @Override
    public Property propertyDTOToProperty(PropertyDTO propertyDTO) {
        return null;
    }

    @Override
    public PartyIdDTO partyIdToPartyIdDTO(PartyId partyId) {
        return null;
    }

    @Override
    public PartyId partyIdDTOToPartyId(PartyIdDTO partyIdDTO) {
        return null;
    }

    @Override
    public PullRequestDTO pullRequestToPullRequestDTO(PullRequest pullRequest) {
        return null;
    }

    @Override
    public PullRequest pullRequestDTOToPullRequest(PullRequestDTO pullRequestDTO) {
        return null;
    }

    @Override
    public PluginUserRO authenticationEntityToPluginUserRO(AuthenticationEntity authenticationEntity) {
        return null;
    }

    @Override
    public AuthenticationEntity pluginUserROToAuthenticationEntity(PluginUserRO pluginUserRO) {
        return null;
    }

    @Override
    public PModeResponseRO pModeArchiveInfoToPModeResponseRO(PModeArchiveInfo pModeArchiveInfo) {
        return null;
    }

    @Override
    public PModeArchiveInfo pModeResponseROToPModeArchiveInfo(PModeResponseRO pModeResponseRO) {
        return null;
    }

    @Override
    public TrustStoreRO trustStoreEntryToTrustStoreRO(TrustStoreEntry trustStoreEntry) {
        return null;
    }

    @Override
    public TrustStoreEntry trustStoreROToTrustStoreEntry(TrustStoreRO trustStoreRO) {
        return null;
    }

    @Override
    public AuditResponseRo auditLogToAuditResponseRo(AuditLog auditLog) {
        return null;
    }

    @Override
    public AuditLog auditResponseRoToAuditLog(AuditResponseRo auditResponseRo) {
        return null;
    }

    @Override
    public DomainRO domainToDomainRO(Domain domain) {
        return null;
    }

    @Override
    public Domain domainROToDomain(DomainRO domain) {
        return null;
    }

    @Override
    public UserResponseRO userToUserResponseRO(User user) {
        return null;
    }

    @Override
    public User userResponseROToUser(UserResponseRO user) {
        return null;
    }

    @Override
    public eu.domibus.core.user.ui.User userApiToUserSecurity(User user) {
        return null;
    }

    @Override
    public User userSecurityToUserApi(eu.domibus.core.user.ui.User user) {
        return null;
    }

    @Override
    public AuditLog auditToAuditLog(Audit audit) {
        return null;
    }

    @Override
    public LoggingLevelRO loggingEntryToLoggingLevelRO(LoggingEntry loggingEntry) {
        return null;
    }

    @Override
    public LoggingEntry loggingLevelROToLoggingEntry(LoggingLevelRO loggingLevelRO) {
        return null;
    }

    @Override
    public Party configurationPartyToParty(eu.domibus.common.model.configuration.Party party) {
        return null;
    }

    @Override
    public eu.domibus.common.model.configuration.Party partyToConfigurationParty(Party party) {
        return null;
    }

    @Override
    public eu.domibus.api.process.Process processRoToProcess(ProcessRo processRo) {
        return null;
    }

    @Override
    public ProcessRo processToProcessRo(eu.domibus.api.process.Process process) {
        return null;
    }

    @Override
    public Alert alertServiceToAlertPersist(eu.domibus.core.alerts.model.service.Alert alert) {
        return null;
    }

    @Override
    public eu.domibus.core.alerts.model.service.Alert alertPersistToAlertService(Alert alert) {
        return null;
    }

    @Override
    public MessageLogInfo messageLogROToMessageLogInfo(MessageLogRO messageLogRO) {
        return null;
    }

    @Override
    public MessageLogRO messageLogInfoToMessageLogRO(MessageLogInfo messageLogInfo) {
        return null;
    }

    @Override
    public CommandEntity commandToCommandEntity(Command command) {
        return null;
    }

    @Override
    public Command commandEntityToCommand(CommandEntity commandEntity) {
        return null;
    }

    @Override
    public CertificateEntrySpi certificateEntryToCertificateEntrySpi(CertificateEntry certificateEntry) {
        return null;
    }

    @Override
    public CertificateEntry certificateEntrySpiToCertificateEntry(CertificateEntrySpi certificateEntrySpi) {
        return null;
    }

    @Override
    public ErrorLogEntry errorLogROToErrorLogEntry(ErrorLogRO errorLogRO) {
        return null;
    }

    @Override
    public ErrorLogRO errorLogEntryToErrorLogRO(ErrorLogEntry errorLogEntry) {
        return null;
    }

    @Override
    public PartyResponseRo partyToPartyResponseRo(Party party) {
        return null;
    }

    @Override
    public Party partyResponseRoToParty(PartyResponseRo partyResponseRo) {
        return null;
    }

    @Override
    public UserMessage userMessageToUserMessageApi(eu.domibus.api.model.UserMessage userMessage) {
        return null;
    }

    @Override
    public eu.domibus.api.model.UserMessage userMessageApiToUserMessage(UserMessage userMessage) {
        return null;
    }

    @Override
    public PasswordEncryptionResultDTO passwordEncryptionResultToPasswordEncryptionResultDTO(PasswordEncryptionResult passwordEncryptionResult) {
        return null;
    }

    @Override
    public List<AuditResponseRo> auditLogListToAuditResponseRoList(List<AuditLog> auditLogList) {
        return null;
    }

    @Override
    public List<AuditLog> auditLogListToAuditList(List<Audit> auditList) {
        return null;
    }

    @Override
    public List<eu.domibus.core.alerts.model.service.Alert> alertPersistListToAlertServiceList(List<Alert> alertList) {
        return null;
    }

    @Override
    public List<AuthenticationEntity> pluginUserROListToAuthenticationEntityList(List<PluginUserRO> pluginUserROList) {
        return null;
    }

    @Override
    public List<BackendFilterEntity> backendFilterListToBackendFilterEntityList(List<BackendFilter> backendFilterList) {
        return null;
    }

    @Override
    public List<BackendFilter> backendFilterEntityListToBackendFilterList(List<BackendFilterEntity> backendFilterEntityList) {
        return null;
    }

    @Override
    public List<BackendFilter> messageFilterROListToBackendFilterList(List<MessageFilterRO> messageFilterROList) {
        return null;
    }

    @Override
    public List<MessageFilterRO> backendFilterListToMessageFilterROList(List<BackendFilter> backendFilterList) {
        return null;
    }

    @Override
    public List<Command> commandEntityListToCommandList(List<CommandEntity> commandEntityList) {
        return null;
    }

    @Override
    public List<DomainRO> domainListToDomainROList(List<Domain> domainList) {
        return null;
    }

    @Override
    public List<ErrorLogRO> errorLogEntryListToErrorLogROList(List<ErrorLogEntry> errorLogEntryList) {
        return null;
    }

    @Override
    public List<MessageAttempt> messageAttemptEntityListToMessageAttemptList(List<MessageAttemptEntity> messageAttemptEntityList) {
        return null;
    }

    @Override
    public List<MessageAttemptEntity> messageAttemptListToMessageAttemptEntityList(List<MessageAttempt> messageAttemptList) {
        return null;
    }

    @Override
    public List<LoggingLevelRO> loggingEntryListToLoggingLevelROList(List<LoggingEntry> loggingEntryList) {
        return null;
    }

    @Override
    public List<Party> configurationPartyListToPartyList(List<eu.domibus.common.model.configuration.Party> configurationPartyList) {
        return null;
    }

    @Override
    public List<eu.domibus.common.model.configuration.Party> partyListToConfigurationPartyList(List<Party> partyList) {
        return null;
    }

    @Override
    public List<PartyResponseRo> partyListToPartyResponseRoList(List<Party> partyList) {
        return null;
    }

    @Override
    public List<Party> partyResponseRoListToPartyList(List<PartyResponseRo> partyResponseRoList) {
        return null;
    }

    @Override
    public List<eu.domibus.api.process.Process> processListToProcessAPIList(List<Process> processList) {
        return null;
    }

    @Override
    public List<eu.domibus.api.process.Process> processRoListToProcessAPIList(List<ProcessRo> processRoList) {
        return null;
    }

    @Override
    public List<ProcessRo> processAPIListToProcessRoList(List<eu.domibus.api.process.Process> processList) {
        return null;
    }

    @Override
    public List<User> userResponseROListToUserList(List<UserResponseRO> userResponseROList) {
        return null;
    }

    @Override
    public List<UserResponseRO> userListToUserResponseROList(List<User> userList) {
        return null;
    }

    @Override
    public List<PModeResponseRO> pModeArchiveInfoListToPModeResponseROList(List<PModeArchiveInfo> pModeArchiveInfoList) {
        return null;
    }

    @Override
    public List<TrustStoreRO> trustStoreEntryListToTrustStoreROList(List<TrustStoreEntry> trustStoreEntryList) {
        return null;
    }
}
