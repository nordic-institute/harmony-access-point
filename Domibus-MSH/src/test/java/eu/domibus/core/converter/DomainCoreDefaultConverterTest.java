package eu.domibus.core.converter;

import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.mapper.EventMapper;
import eu.domibus.core.alerts.model.mapper.EventMapperImpl;
import eu.domibus.core.alerts.model.mapper.EventMapperImpl_;
import eu.domibus.core.audit.model.mapper.AuditMapper;
import eu.domibus.core.audit.model.mapper.AuditMapperImpl;
import eu.domibus.core.util.DateUtilImpl;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author Ioana Dragusanu
 * @since 4.1
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class DomainCoreDefaultConverterTest {

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
        public DomainCoreConverter domainCoreConverter() {
            DomainCoreConverter domainCoreConverter = new DomainCoreDefaultConverter();
            return domainCoreConverter;
        }

        @Bean
        public DomibusCoreMapper domibusCoreMapper() {
            DomibusCoreMapper domibusCoreMapper = new DomibusCoreMapperImpl();
            return domibusCoreMapper;
        }
    }

    @Autowired
    DomainCoreConverter domainCoreConverter;

    @Autowired
    DomibusCoreMapper domibusCoreMapper;

    @Autowired
    ObjectService objectService;

//    @Test
//    public void testConvertPartyResponseRo() throws Exception {
//        PartyResponseRo toConvert = (PartyResponseRo) objectService.createInstance(PartyResponseRo.class);
//        final Party converted = domainCoreConverter.convert(toConvert, Party.class);
//        final PartyResponseRo convertedBack = domainCoreConverter.convert(converted, PartyResponseRo.class);
//        // these fields are missing in Party, fill them so the assertion works
//        convertedBack.setJoinedIdentifiers(toConvert.getJoinedIdentifiers());
//        convertedBack.setJoinedProcesses(toConvert.getJoinedProcesses());
//        convertedBack.setCertificateContent(toConvert.getCertificateContent());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertParty() throws Exception {
//        Party toConvert = (Party) objectService.createInstance(Party.class);
//        final PartyResponseRo converted = domainCoreConverter.convert(toConvert, PartyResponseRo.class);
//        final Party convertedBack = domainCoreConverter.convert(converted, Party.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertDomain() throws Exception {
//        DomainSpi toConvert = (DomainSpi) objectService.createInstance(DomainSpi.class);
//        final Domain converted = domainCoreConverter.convert(toConvert, Domain.class);
//        final DomainSpi convertedBack = domainCoreConverter.convert(converted, DomainSpi.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertBackendFilter() throws Exception {
//        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
//        final MessageFilterRO converted = domainCoreConverter.convert(toConvert, MessageFilterRO.class);
//        final BackendFilter convertedBack = domainCoreConverter.convert(converted, BackendFilter.class);
//        convertedBack.setActive(true);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertBackendFilterEntity() throws Exception {
//        BackendFilter toConvert = (BackendFilter) objectService.createInstance(BackendFilter.class);
//        final BackendFilterEntity converted = domainCoreConverter.convert(toConvert, BackendFilterEntity.class);
//        final BackendFilter convertedBack = domainCoreConverter.convert(converted, BackendFilter.class);
//        convertedBack.setActive(true);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertRoutingCriteria() throws Exception {
//        RoutingCriteria toConvert = (RoutingCriteria) objectService.createInstance(RoutingCriteria.class);
//        final RoutingCriteriaEntity converted = domainCoreConverter.convert(toConvert, RoutingCriteriaEntity.class);
//        final RoutingCriteria convertedBack = domainCoreConverter.convert(converted, RoutingCriteria.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertMessageAttemptEntity() throws Exception {
//        MessageAttemptEntity toConvert = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
//        final MessageAttempt converted = domainCoreConverter.convert(toConvert, MessageAttempt.class);
//        final MessageAttemptEntity convertedBack = domainCoreConverter.convert(converted, MessageAttemptEntity.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertPartProperties() throws Exception {
//        PartPropertiesDTO toConvert = (PartPropertiesDTO) objectService.createInstance(PartPropertiesDTO.class);
//        final PartProperties converted = domainCoreConverter.convert(toConvert, PartProperties.class);
//        final PartPropertiesDTO convertedBack = domainCoreConverter.convert(converted, PartPropertiesDTO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertProcess() throws Exception {
//        Process toConvert = (Process) objectService.createInstance(Process.class);
//        final eu.domibus.common.model.configuration.Process converted = domainCoreConverter.convert(toConvert, eu.domibus.common.model.configuration.Process.class);
//        final Process convertedBack = domainCoreConverter.convert(converted, Process.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUserMessage() throws Exception {
//        UserMessageDTO toConvert = (UserMessageDTO) objectService.createInstance(UserMessageDTO.class);
//        final UserMessage converted = domainCoreConverter.convert(toConvert, UserMessage.class);
//        final UserMessageDTO convertedBack = domainCoreConverter.convert(converted, UserMessageDTO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUserMessageDTO() throws Exception {
//        PullRequestDTO toConvert = (PullRequestDTO) objectService.createInstance(PullRequestDTO.class);
//        final PullRequest converted = domainCoreConverter.convert(toConvert, PullRequest.class);
//        final PullRequestDTO convertedBack = domainCoreConverter.convert(converted, PullRequestDTO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertPluginUserRO() throws Exception {
//        PluginUserRO toConvert = (PluginUserRO) objectService.createInstance(PluginUserRO.class);
//        toConvert.setCertificateId(null);
//        toConvert.setAuthenticationType(AuthType.BASIC.name());
//        final AuthenticationEntity converted = domainCoreConverter.convert(toConvert, AuthenticationEntity.class);
//        final PluginUserRO convertedBack = domainCoreConverter.convert(converted, PluginUserRO.class);
//        convertedBack.setSuspended(true);
//        convertedBack.setStatus("status");
//        convertedBack.setAuthenticationType(AuthType.BASIC.name());
//        convertedBack.setDomain("domain");
//
//        Gson gson = new Gson();
//        Assert.assertEquals(gson.toJson(toConvert), gson.toJson(convertedBack));
//    }
//
//    @Test
//    public void testConvertEvent() throws Exception {
//        PModeResponseRO toConvert = (PModeResponseRO) objectService.createInstance(PModeResponseRO.class);
//        final PModeArchiveInfo converted = domainCoreConverter.convert(toConvert, PModeArchiveInfo.class);
//        final PModeResponseRO convertedBack = domainCoreConverter.convert(converted, PModeResponseRO.class);
//        convertedBack.setCurrent(true);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertTrustStore() throws Exception {
//        TrustStoreRO toConvert = (TrustStoreRO) objectService.createInstance(TrustStoreRO.class);
//        final TrustStoreEntry converted = domainCoreConverter.convert(toConvert, TrustStoreEntry.class);
//        final TrustStoreRO convertedBack = domainCoreConverter.convert(converted, TrustStoreRO.class);
//        convertedBack.setValidUntil(toConvert.getValidUntil());
//        convertedBack.setValidFrom(toConvert.getValidFrom());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertAuditResponse() throws Exception {
//        AuditResponseRo toConvert = (AuditResponseRo) objectService.createInstance(AuditResponseRo.class);
//        final AuditLog converted = domainCoreConverter.convert(toConvert, AuditLog.class);
//        final AuditResponseRo convertedBack = domainCoreConverter.convert(converted, AuditResponseRo.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertDomainRO() throws Exception {
//        DomainRO toConvert = (DomainRO) objectService.createInstance(DomainRO.class);
//        final Domain converted = domainCoreConverter.convert(toConvert, Domain.class);
//        final DomainRO convertedBack = domainCoreConverter.convert(converted, DomainRO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertDomainToDomainDTO() throws Exception {
//        Domain toConvert = (Domain) objectService.createInstance(Domain.class);
//        final DomainDTO converted = domainCoreConverter.convert(toConvert, DomainDTO.class);
//        final Domain convertedBack = domainCoreConverter.convert(converted, Domain.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertDomainDTOToDomain() throws Exception {
//        DomainDTO toConvert = (DomainDTO) objectService.createInstance(DomainDTO.class);
//        final Domain converted = domainCoreConverter.convert(toConvert, Domain.class);
//        final DomainDTO convertedBack = domainCoreConverter.convert(converted, DomainDTO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUserResponseRO() throws Exception {
//        UserResponseRO toConvert = (UserResponseRO) objectService.createInstance(UserResponseRO.class);
//        final User converted = domainCoreConverter.convert(toConvert, User.class);
//        final UserResponseRO convertedBack = domainCoreConverter.convert(converted, UserResponseRO.class);
//        toConvert.updateRolesField();
//        convertedBack.updateRolesField();
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUser() throws Exception {
//        User toConvert = (User) objectService.createInstance(User.class);
//        final eu.domibus.core.user.ui.User converted = domainCoreConverter.convert(toConvert, eu.domibus.core.user.ui.User.class);
//        final User convertedBack = domainCoreConverter.convert(converted, User.class);
//        convertedBack.setDomain(toConvert.getDomain());
//        convertedBack.setStatus(toConvert.getStatus());
//        convertedBack.setAuthorities(toConvert.getAuthorities());
//        convertedBack.setSuspended(true);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertAuditLog() throws Exception {
//        AuditLog toConvert = (AuditLog) objectService.createInstance(AuditLog.class);
//        final Audit converted = domainCoreConverter.convert(toConvert, Audit.class);
//        final AuditLog convertedBack = domainCoreConverter.convert(converted, AuditLog.class);
//        objectService.assertObjects(convertedBack.getUser(), toConvert.getUser());
//        objectService.assertObjects(convertedBack.getChanged(), toConvert.getChanged());
//        objectService.assertObjects(convertedBack.getId(), toConvert.getId());
//        objectService.assertObjects(convertedBack.getAction(), toConvert.getAction());
//        objectService.assertObjects(convertedBack.getRevisionId(), toConvert.getRevisionId());
//        objectService.assertObjects(convertedBack.getAuditTargetName(), toConvert.getAuditTargetName());
//    }
//
//    @Test
//    public void testConvertLoggingLevelRO() throws Exception {
//        LoggingLevelRO toConvert = (LoggingLevelRO) objectService.createInstance(LoggingLevelRO.class);
//        final LoggingEntry converted = domainCoreConverter.convert(toConvert, LoggingEntry.class);
//        final LoggingLevelRO convertedBack = domainCoreConverter.convert(converted, LoggingLevelRO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertConfigurationParty() throws Exception {
//        Party toConvert = (Party) objectService.createInstance(Party.class);
//        final eu.domibus.common.model.configuration.Party converted = domainCoreConverter.convert(toConvert, eu.domibus.common.model.configuration.Party.class);
//        final Party convertedBack = domainCoreConverter.convert(converted, Party.class);
//        convertedBack.setEntityId(toConvert.getEntityId());
//        convertedBack.setProcessesWithPartyAsInitiator(toConvert.getProcessesWithPartyAsInitiator());
//        convertedBack.setProcessesWithPartyAsResponder(toConvert.getProcessesWithPartyAsResponder());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertProcessRo() throws Exception {
//        ProcessRo toConvert = (ProcessRo) objectService.createInstance(ProcessRo.class);
//        final Process converted = domainCoreConverter.convert(toConvert, Process.class);
//        final ProcessRo convertedBack = domainCoreConverter.convert(converted, ProcessRo.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertMessageLogRO() throws Exception {
//        MessageLogRO toConvert = (MessageLogRO) objectService.createInstance(MessageLogRO.class);
//        final MessageLogInfo converted = domainCoreConverter.convert(toConvert, MessageLogInfo.class);
//        final MessageLogRO convertedBack = domainCoreConverter.convert(converted, MessageLogRO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUIMessageEntity() throws Exception {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final UIMessageDiffEntity converted = domainCoreConverter.convert(toConvert, UIMessageDiffEntity.class);
//        final UIMessageEntity convertedBack = domainCoreConverter.convert(converted, UIMessageEntity.class);
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setEntityId(toConvert.getEntityId());
//        convertedBack.setAction(toConvert.getAction());
//        convertedBack.setServiceType(toConvert.getServiceType());
//        convertedBack.setServiceValue(toConvert.getServiceValue());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUIMessageEntityMessageLog() throws Exception {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final MessageLogRO converted = domainCoreConverter.convert(toConvert, MessageLogRO.class);
//        final UIMessageEntity convertedBack = domainCoreConverter.convert(converted, UIMessageEntity.class);
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setEntityId(toConvert.getEntityId());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUIMessageEntityMessageLogInfo() throws Exception {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final MessageLogInfo converted = domainCoreConverter.convert(toConvert, MessageLogInfo.class);
//        final UIMessageEntity convertedBack = domainCoreConverter.convert(converted, UIMessageEntity.class);
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setEntityId(toConvert.getEntityId());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUIMessageEntitySignalMessageLog() throws Exception {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final SignalMessageLog converted = domainCoreConverter.convert(toConvert, SignalMessageLog.class);
//        final UIMessageEntity convertedBack = domainCoreConverter.convert(converted, UIMessageEntity.class);
//        convertedBack.setConversationId(toConvert.getConversationId());
//        convertedBack.setRefToMessageId(toConvert.getRefToMessageId());
//        convertedBack.setFromId(toConvert.getFromId());
//        convertedBack.setToId(toConvert.getToId());
//        convertedBack.setFromScheme(toConvert.getFromScheme());
//        convertedBack.setToScheme(toConvert.getToScheme());
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setServiceType(toConvert.getServiceType());
//        convertedBack.setServiceValue(toConvert.getServiceValue());
//        convertedBack.setAction(toConvert.getAction());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUIMessageEntityUserMessageLog() throws Exception {
//        UIMessageEntity toConvert = (UIMessageEntity) objectService.createInstance(UIMessageEntity.class);
//        final UserMessageLog converted = domainCoreConverter.convert(toConvert, UserMessageLog.class);
//        final UIMessageEntity convertedBack = domainCoreConverter.convert(converted, UIMessageEntity.class);
//        convertedBack.setConversationId(toConvert.getConversationId());
//        convertedBack.setFromId(toConvert.getFromId());
//        convertedBack.setToId(toConvert.getToId());
//        convertedBack.setFromScheme(toConvert.getFromScheme());
//        convertedBack.setToScheme(toConvert.getToScheme());
//        convertedBack.setLastModified(toConvert.getLastModified());
//        convertedBack.setAction(toConvert.getAction());
//        convertedBack.setServiceType(toConvert.getServiceType());
//        convertedBack.setServiceValue(toConvert.getServiceValue());
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertCommand() throws Exception {
//        Command toConvert = (Command) objectService.createInstance(Command.class);
//        final CommandEntity converted = domainCoreConverter.convert(toConvert, CommandEntity.class);
//        final Command convertedBack = domainCoreConverter.convert(converted, Command.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertCertificateEntry() throws Exception {
//        CertificateEntry toConvert = new CertificateEntry();
//        toConvert.setAlias("alias1");
//        toConvert.setCertificate(new X509CertImpl());
//        final CertificateEntrySpi converted = domainCoreConverter.convert(toConvert, CertificateEntrySpi.class);
//        final CertificateEntry convertedBack = domainCoreConverter.convert(converted, CertificateEntry.class);
//        objectService.assertObjects(convertedBack.getAlias(), toConvert.getAlias());
//    }
//
//    @Test
//    public void testConvertErrorLogRO() throws Exception {
//        ErrorLogRO toConvert = (ErrorLogRO) objectService.createInstance(ErrorLogRO.class);
//        final ErrorLogEntry converted = domainCoreConverter.convert(toConvert, ErrorLogEntry.class);
//        final ErrorLogRO convertedBack = domainCoreConverter.convert(converted, ErrorLogRO.class);
//        objectService.assertObjects(convertedBack, toConvert);
//    }
//
//    @Test
//    public void testConvertUserMessageApi() throws Exception {
//        CollaborationInfo collaborationInfo = (CollaborationInfo) objectService.createInstance(CollaborationInfo.class);
//        eu.domibus.api.usermessage.domain.UserMessage toConvert = new eu.domibus.api.usermessage.domain.UserMessage();
//        toConvert.setCollaborationInfo(collaborationInfo);
//        final UserMessage converted = domainCoreConverter.convert(toConvert, UserMessage.class);
//        final eu.domibus.api.usermessage.domain.UserMessage convertedBack = domainCoreConverter.convert(converted, eu.domibus.api.usermessage.domain.UserMessage.class);
//        objectService.assertObjects(convertedBack.getCollaborationInfo(), toConvert.getCollaborationInfo());
//    }
//
//    @Test
//    public void testConvertList() throws Exception {
//        MessageAttemptEntity toConvert1 = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
//        MessageAttemptEntity toConvert2 = (MessageAttemptEntity) objectService.createInstance(MessageAttemptEntity.class);
//        List<MessageAttemptEntity> toConvertList = new ArrayList<>();
//        toConvertList.add(toConvert1);
//        toConvertList.add(toConvert2);
//        final List<MessageAttempt> convertedList = domainCoreConverter.convert(toConvertList, MessageAttempt.class);
//        final List<MessageAttemptEntity> convertedBackList = domainCoreConverter.convert(convertedList, MessageAttemptEntity.class);
//        objectService.assertObjects(convertedBackList, toConvertList);
//    }
//
//    @Test(expected = ConverterException.class)
//    public void testConvertPropertyNotCalled() throws Exception {
//        PropertyDTO toConvert = (PropertyDTO) objectService.createInstance(PropertyDTO.class);
//        domainCoreConverter.convert(toConvert, Property.class);
//    }
}
