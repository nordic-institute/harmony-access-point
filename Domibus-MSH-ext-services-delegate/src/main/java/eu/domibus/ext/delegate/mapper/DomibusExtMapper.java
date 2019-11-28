package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.monitoring.DomibusMonitoringInfo;
import eu.domibus.api.monitoring.QuartzInfo;
import eu.domibus.api.monitoring.ServiceInfo;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.domain.*;
import org.mapstruct.DecoratedWith;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Ioana Dragusanu (idragusa)
 * @since 4.1
 */
@Mapper(uses = MonitoringMapper.class, componentModel = "spring")
public interface DomibusExtMapper {
    DomainDTO domainToDomainDTO(Domain domain);

    Domain domainDTOToDomain(DomainDTO domain);

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);

    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);

    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

    JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage);

    JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO);

    UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO);

    UserMessageDTO userMessageToUserMessageDTO(UserMessage userMessage);

    PModeArchiveInfoDTO pModeArchiveInfoToPModeArchiveInfoDto(PModeArchiveInfo pModeArchiveInfo);

    PasswordEncryptionResultDTO passwordEncryptionResultToPasswordEncryptionResultDTO(PasswordEncryptionResult passwordEncryptionResult);

    DomibusPropertyMetadataDTO domibusPropertyMetadataToDomibusPropertyMetadataDTO(DomibusPropertyMetadata domibusPropertyMetadata);

    DomibusPropertyMetadata domibusPropertyMetadataDTOToDomibusPropertyMetadata(DomibusPropertyMetadataDTO domibusPropertyMetadata);

  /*  @Mapping(source = "dataBaseInfo", target = "dataBaseInfoDTO")
    @Mapping(source = "jmsBrokerInfo", target = "jmsBrokerInfoDTO")*/
   // @Mapping(source = "quartzInfo", target = "quartzInfoDTO")
   // @Mapping(source = "serviceInfo", target = "serviceInfoDTO")
    //ServiceInfoDTO serviceInfoToServiceInfoDTO(ServiceInfo serviceInfo);
   // @InheritInverseConfiguration
    //ServiceInfo serviceInfoDTOToServiceInfo(ServiceInfoDTO serviceInfoDTO);
    // @Mapping(target = "services", ignore = true)
   // DomibusMonitoringInfoDTO domibusMonitoringInfoToDomibusMonitoringInfoDTO(DomibusMonitoringInfo domibusMonitoringInfo);

   //  @InheritInverseConfiguration
  //  DomibusMonitoringInfo domibusMonitoringInfoDTOToDomibusMonitoringInfo(DomibusMonitoringInfoDTO domibusMonitoringInfoDTO);
    /*@Mapping(source = "quartzTriggerInfos", target = "quartzTriggerInfoDTOS")
    QuartzInfoDTO quartzInfoToQuartzInfoDTO(QuartzInfo quartzInfo);*/
}
