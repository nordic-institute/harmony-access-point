package eu.domibus.core.converter;

import com.google.gson.Gson;
import eu.domibus.api.audit.AuditLog;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.pki.CertificateEntry;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.process.Process;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.User;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.mapper.EventMapperImpl;
import eu.domibus.core.alerts.model.mapper.EventMapperImpl_;
import eu.domibus.core.audit.model.Audit;
import eu.domibus.core.audit.model.mapper.AuditMapper;
import eu.domibus.core.audit.model.mapper.AuditMapperImpl;
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
import eu.domibus.core.replication.UIMessageDiffEntity;
import eu.domibus.core.replication.UIMessageEntity;
import eu.domibus.core.user.plugin.AuthenticationEntity;
import eu.domibus.core.util.DateUtilImpl;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.PartPropertiesDTO;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.web.rest.ro.*;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import sun.security.x509.X509CertImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * IT test for DomibusCoreMapper, AuditMapper and EventMapper
 * @since 5.0
 * @author Ioana Dragusanu
 * @author Catalin Enache
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DomibusCoreAuditEventMapperIT {

    @Configuration
    @ImportResource({
            "classpath:config/commonsTestContext.xml"
    })
    static class ContextConfiguration {

        @Bean
        public DateUtil dateUtil() {
            return new DateUtilImpl();
        }

        @Bean
        @Qualifier("delegate")
        public EventMapper eventMapper() {
            EventMapperImpl eventMapper = new EventMapperImpl();
            eventMapper.setDelegate(new EventMapperImpl_());
            return eventMapper;
        }

        @Bean
        public AuditMapper auditMapper() {
            AuditMapperImpl auditMapper = new AuditMapperImpl();
            return auditMapper;
        }

        @Bean
        public DomibusCoreMapper domibusCoreMapper() {
            DomibusCoreMapper domibusCoreMapper = new DomibusCoreMapperImpl();
            return domibusCoreMapper;
        }
    }

    @Autowired
    DomibusCoreMapper domibusCoreMapper;

    @Autowired
    AuditMapper auditMapper;

    @Autowired
    ObjectService objectService;

    @Test
    public void testConvertPartyResponseRo() throws Exception {
        PartyResponseRo toConvert = (PartyResponseRo) objectService.createInstance(PartyResponseRo.class);
        final Party converted = domibusCoreMapper.partyResponseRoToParty(toConvert);
        final PartyResponseRo convertedBack = domibusCoreMapper.partyToPartyResponseRo(converted);
        // these fields are missing in Party, fill them so the assertion works
        convertedBack.setJoinedIdentifiers(toConvert.getJoinedIdentifiers());
        convertedBack.setJoinedProcesses(toConvert.getJoinedProcesses());
        convertedBack.setCertificateContent(toConvert.getCertificateContent());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertParty() throws Exception {
        Party toConvert = (Party) objectService.createInstance(Party.class);
        final PartyResponseRo converted = domibusCoreMapper.partyToPartyResponseRo(toConvert);
        final Party convertedBack = domibusCoreMapper.partyResponseRoToParty(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertDomain() throws Exception {
        DomainSpi toConvert = (DomainSpi) objectService.createInstance(DomainSpi.class);
        final Domain converted = domibusCoreMapper.domainSpiToDomain(toConvert);
        final DomainSpi convertedBack = domibusCoreMapper.domainToDomainSpi(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertBackendFilter() throws Exception {
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        final MessageFilterRO converted = domibusCoreMapper.backendFilterToMessageFilterRO(toConvert);
        final BackendFilter convertedBack = domibusCoreMapper.messageFilterROToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertBackendFilterEntity() throws Exception {
        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
        final BackendFilterEntity converted = domibusCoreMapper.backendFilterToBackendFilterEntity(toConvert);
        final BackendFilter convertedBack = domibusCoreMapper.backendFilterEntityToBackendFilter(converted);
        convertedBack.setActive(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertRoutingCriteria() throws Exception {
        RoutingCriteria toConvert = (RoutingCriteria) objectService.createInstance(RoutingCriteria.class);
        final RoutingCriteriaEntity converted = domibusCoreMapper.routingCriteriaToRoutingCriteriaEntity(toConvert);
        final RoutingCriteria convertedBack = domibusCoreMapper.routingCriteriaEntityToRoutingCriteria(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertMessageAttemptEntity() throws Exception {
        MessageAttemptEntity toConvert = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
        final MessageAttempt converted = domibusCoreMapper.messageAttemptEntityToMessageAttempt(toConvert);
        final MessageAttemptEntity convertedBack = domibusCoreMapper.messageAttemptToMessageAttemptEntity(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertPartProperties() throws Exception {
        PartPropertiesDTO toConvert = (PartPropertiesDTO) objectService.createInstance(PartPropertiesDTO.class);
        final PartProperties converted = domibusCoreMapper.partPropertiesDTOToPartProperties(toConvert);
        final PartPropertiesDTO convertedBack = domibusCoreMapper.partPropertiesToPartPropertiesDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertProcess() throws Exception {
        Process toConvert = (Process) objectService.createInstance(Process.class);
        final eu.domibus.common.model.configuration.Process converted = domibusCoreMapper.processAPIToProcess(toConvert);
        final Process convertedBack = domibusCoreMapper.processToProcessAPI(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUserMessage() throws Exception {
        UserMessageDTO toConvert = (UserMessageDTO) objectService.createInstance(UserMessageDTO.class);
        final UserMessage converted = domibusCoreMapper.userMessageDTOToUserMessage(toConvert);
        final UserMessageDTO convertedBack = domibusCoreMapper.userMessageToUserMessageDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUserMessageDTO() throws Exception {
        PullRequestDTO toConvert = (PullRequestDTO) objectService.createInstance(PullRequestDTO.class);
        final PullRequest converted = domibusCoreMapper.pullRequestDTOToPullRequest(toConvert);
        final PullRequestDTO convertedBack = domibusCoreMapper.pullRequestToPullRequestDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertPluginUserRO() throws Exception {
        PluginUserRO toConvert = (PluginUserRO) objectService.createInstance(PluginUserRO.class);
        toConvert.setCertificateId(null);
        toConvert.setAuthenticationType(AuthType.BASIC.name());
        final AuthenticationEntity converted = domibusCoreMapper.pluginUserROToAuthenticationEntity(toConvert);
        final PluginUserRO convertedBack = domibusCoreMapper.authenticationEntityToPluginUserRO(converted);
        convertedBack.setSuspended(true);
        convertedBack.setStatus("status");
        convertedBack.setAuthenticationType(AuthType.BASIC.name());
        convertedBack.setDomain("domain");

        Gson gson = new Gson();
        Assert.assertEquals(gson.toJson(toConvert), gson.toJson(convertedBack));
    }

    @Test
    public void testConvertEvent() throws Exception {
        PModeResponseRO toConvert = (PModeResponseRO) objectService.createInstance(PModeResponseRO.class);
        final PModeArchiveInfo converted = domibusCoreMapper.pModeResponseROToPModeArchiveInfo(toConvert);
        final PModeResponseRO convertedBack = domibusCoreMapper.pModeArchiveInfoToPModeResponseRO(converted);
        convertedBack.setCurrent(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertTrustStore() throws Exception {
        TrustStoreRO toConvert = (TrustStoreRO) objectService.createInstance(TrustStoreRO.class);
        final TrustStoreEntry converted = domibusCoreMapper.trustStoreROToTrustStoreEntry(toConvert);
        final TrustStoreRO convertedBack = domibusCoreMapper.trustStoreEntryToTrustStoreRO(converted);
        convertedBack.setValidUntil(toConvert.getValidUntil());
        convertedBack.setValidFrom(toConvert.getValidFrom());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertAuditResponse() throws Exception {
        AuditResponseRo toConvert = (AuditResponseRo) objectService.createInstance(AuditResponseRo.class);
        final AuditLog converted = domibusCoreMapper.auditResponseRoToAuditLog(toConvert);
        final AuditResponseRo convertedBack = domibusCoreMapper.auditLogToAuditResponseRo(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertDomainRO() throws Exception {
        DomainRO toConvert = (DomainRO) objectService.createInstance(DomainRO.class);
        final Domain converted = domibusCoreMapper.domainROToDomain(toConvert);
        final DomainRO convertedBack = domibusCoreMapper.domainToDomainRO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertDomainToDomainDTO() throws Exception {
        Domain toConvert = (Domain) objectService.createInstance(Domain.class);
        final DomainDTO converted = domibusCoreMapper.domainToDomainDTO(toConvert);
        final Domain convertedBack = domibusCoreMapper.domainDTOToDomain(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertDomainDTOToDomain() throws Exception {
        DomainDTO toConvert = (DomainDTO) objectService.createInstance(DomainDTO.class);
        final Domain converted = domibusCoreMapper.domainDTOToDomain(toConvert);
        final DomainDTO convertedBack = domibusCoreMapper.domainToDomainDTO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUserResponseRO() throws Exception {
        UserResponseRO toConvert = (UserResponseRO) objectService.createInstance(UserResponseRO.class);
        final User converted = domibusCoreMapper.userResponseROToUser(toConvert);
        final UserResponseRO convertedBack = domibusCoreMapper.userToUserResponseRO(converted);
        toConvert.updateRolesField();
        convertedBack.updateRolesField();
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUser() throws Exception {
        User toConvert = (User) objectService.createInstance(User.class);
        final eu.domibus.core.user.ui.User converted = domibusCoreMapper.userApiToUserSecurity(toConvert);
        final User convertedBack = domibusCoreMapper.userSecurityToUserApi(converted);
        convertedBack.setDomain(toConvert.getDomain());
        convertedBack.setStatus(toConvert.getStatus());
        convertedBack.setAuthorities(toConvert.getAuthorities());
        convertedBack.setSuspended(true);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertAuditLog() throws Exception {
        AuditLog toConvert = (AuditLog) objectService.createInstance(AuditLog.class);
        final Audit converted = auditMapper.auditLogToAudit(toConvert);
        final AuditLog convertedBack = domibusCoreMapper.auditToAuditLog(converted);
        objectService.assertObjects(convertedBack.getUser(), toConvert.getUser());
        objectService.assertObjects(convertedBack.getChanged(), toConvert.getChanged());
        objectService.assertObjects(convertedBack.getId(), toConvert.getId());
        objectService.assertObjects(convertedBack.getAction(), toConvert.getAction());
        objectService.assertObjects(convertedBack.getRevisionId(), toConvert.getRevisionId());
        objectService.assertObjects(convertedBack.getAuditTargetName(), toConvert.getAuditTargetName());
    }

    @Test
    public void testConvertLoggingLevelRO() throws Exception {
        LoggingLevelRO toConvert = (LoggingLevelRO) objectService.createInstance(LoggingLevelRO.class);
        final LoggingEntry converted = domibusCoreMapper.loggingLevelROToLoggingEntry(toConvert);
        final LoggingLevelRO convertedBack = domibusCoreMapper.loggingEntryToLoggingLevelRO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertConfigurationParty() throws Exception {
        Party toConvert = (Party) objectService.createInstance(Party.class);
        final eu.domibus.common.model.configuration.Party converted = domibusCoreMapper.partyToConfigurationParty(toConvert);
        final Party convertedBack = domibusCoreMapper.configurationPartyToParty(converted);
        convertedBack.setEntityId(toConvert.getEntityId());
        convertedBack.setProcessesWithPartyAsInitiator(toConvert.getProcessesWithPartyAsInitiator());
        convertedBack.setProcessesWithPartyAsResponder(toConvert.getProcessesWithPartyAsResponder());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertProcessRo() throws Exception {
        ProcessRo toConvert = (ProcessRo) objectService.createInstance(ProcessRo.class);
        final Process converted = domibusCoreMapper.processRoToProcess(toConvert);
        final ProcessRo convertedBack = domibusCoreMapper.processToProcessRo(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertMessageLogRO() throws Exception {
        MessageLogRO toConvert = (MessageLogRO) objectService.createInstance(MessageLogRO.class);
        final MessageLogInfo converted = domibusCoreMapper.messageLogROToMessageLogInfo(toConvert);
        final MessageLogRO convertedBack = domibusCoreMapper.messageLogInfoToMessageLogRO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUIMessageEntity() throws Exception {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final UIMessageDiffEntity converted = domibusCoreMapper.uiMessageEntityToUIMessageDiffEntity(toConvert);
        final UIMessageEntity convertedBack = domibusCoreMapper.uiMessageDiffEntityToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        convertedBack.setAction(toConvert.getAction());
        convertedBack.setServiceType(toConvert.getServiceType());
        convertedBack.setServiceValue(toConvert.getServiceValue());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUIMessageEntityMessageLog() throws Exception {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final MessageLogRO converted = domibusCoreMapper.uiMessageEntityToMessageLogRO(toConvert);
        final UIMessageEntity convertedBack = domibusCoreMapper.messageLogROToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUIMessageEntityMessageLogInfo() throws Exception {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final MessageLogInfo converted = domibusCoreMapper.uiMessageEntityToMessageLogInfo(toConvert);
        final UIMessageEntity convertedBack = domibusCoreMapper.messageLogInfoToUIMessageEntity(converted);
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setEntityId(toConvert.getEntityId());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUIMessageEntitySignalMessageLog() throws Exception {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final SignalMessageLog converted = domibusCoreMapper.uiMessageEntityToSignalMessageLog(toConvert);
        final UIMessageEntity convertedBack = domibusCoreMapper.signalMessageLogToUIMessageEntity(converted);
        convertedBack.setConversationId(toConvert.getConversationId());
        convertedBack.setRefToMessageId(toConvert.getRefToMessageId());
        convertedBack.setFromId(toConvert.getFromId());
        convertedBack.setToId(toConvert.getToId());
        convertedBack.setFromScheme(toConvert.getFromScheme());
        convertedBack.setToScheme(toConvert.getToScheme());
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setServiceType(toConvert.getServiceType());
        convertedBack.setServiceValue(toConvert.getServiceValue());
        convertedBack.setAction(toConvert.getAction());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUIMessageEntityUserMessageLog() throws Exception {
        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
        final UserMessageLog converted = domibusCoreMapper.uiMessageEntityToUserMessageLog(toConvert);
        final UIMessageEntity convertedBack = domibusCoreMapper.userMessageLogToUIMessageEntity(converted);
        convertedBack.setConversationId(toConvert.getConversationId());
        convertedBack.setFromId(toConvert.getFromId());
        convertedBack.setToId(toConvert.getToId());
        convertedBack.setFromScheme(toConvert.getFromScheme());
        convertedBack.setToScheme(toConvert.getToScheme());
        convertedBack.setLastModified(toConvert.getLastModified());
        convertedBack.setAction(toConvert.getAction());
        convertedBack.setServiceType(toConvert.getServiceType());
        convertedBack.setServiceValue(toConvert.getServiceValue());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertCommand() throws Exception {
        Command toConvert = (Command) objectService.createInstance(Command.class);
        final CommandEntity converted = domibusCoreMapper.commandToCommandEntity(toConvert);
        final Command convertedBack = domibusCoreMapper.commandEntityToCommand(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertCertificateEntry() throws Exception {
        CertificateEntry toConvert = new CertificateEntry();
        toConvert.setAlias("alias1");
        toConvert.setCertificate(new X509CertImpl());
        final CertificateEntrySpi converted = domibusCoreMapper.certificateEntryToCertificateEntrySpi(toConvert);
        final CertificateEntry convertedBack = domibusCoreMapper.certificateEntrySpiToCertificateEntry(converted);
        objectService.assertObjects(convertedBack.getAlias(), toConvert.getAlias());
    }

    @Test
    public void testConvertErrorLogRO() throws Exception {
        ErrorLogRO toConvert = (ErrorLogRO) objectService.createInstance(ErrorLogRO.class);
        final ErrorLogEntry converted = domibusCoreMapper.errorLogROToErrorLogEntry(toConvert);
        final ErrorLogRO convertedBack = domibusCoreMapper.errorLogEntryToErrorLogRO(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void testConvertUserMessageApi() throws Exception {
        eu.domibus.api.usermessage.domain.CollaborationInfo collaborationInfo = (eu.domibus.api.usermessage.domain.CollaborationInfo) objectService.createInstance(eu.domibus.api.usermessage.domain.CollaborationInfo.class);
        eu.domibus.api.usermessage.domain.UserMessage toConvert = new eu.domibus.api.usermessage.domain.UserMessage();
        toConvert.setCollaborationInfo(collaborationInfo);
        final UserMessage converted = domibusCoreMapper.userMessageApiToUserMessage(toConvert);
        final eu.domibus.api.usermessage.domain.UserMessage convertedBack = domibusCoreMapper.userMessageToUserMessageApi(converted);
        objectService.assertObjects(convertedBack.getCollaborationInfo(), toConvert.getCollaborationInfo());
    }

    @Test
    public void testConvertList() throws Exception {
        MessageAttemptEntity toConvert1 = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
        MessageAttemptEntity toConvert2 = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
        List<MessageAttemptEntity> toConvertList = new ArrayList<>();
        toConvertList.add(toConvert1);
        toConvertList.add(toConvert2);
        final List<MessageAttempt> convertedList = domibusCoreMapper.messageAttemptEntityListToMessageAttemptList(toConvertList);
        final List<MessageAttemptEntity> convertedBackList = domibusCoreMapper.messageAttemptListToMessageAttemptEntityList(convertedList);
        objectService.assertObjects(convertedBackList, toConvertList);
    }


}