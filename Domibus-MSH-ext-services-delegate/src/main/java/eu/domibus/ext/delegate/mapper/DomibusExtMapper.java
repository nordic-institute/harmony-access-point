package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.acknowledge.MessageAcknowledgement;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Identifier;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.process.Process;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionResult;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.domain.*;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Ioana Dragusanu (idragusa), azhikso
 * @since 4.1
 */
@Mapper(uses = MonitoringMapper.class, componentModel = "spring")
@DecoratedWith(DomibusExtMapperDecorator.class)
public interface DomibusExtMapper {

    DomainDTO domainToDomainDTO(Domain domain);

    Domain domainDTOToDomain(DomainDTO domain);

    MessageAttemptDTO messageAttemptToMessageAttemptDTO(MessageAttempt messageAttempt);

    MessageAttempt messageAttemptDTOToMessageAttempt(MessageAttemptDTO messageAttemptDTO);

    MessageAcknowledgementDTO messageAcknowledgementToMessageAcknowledgementDTO(MessageAcknowledgement messageAcknowledgementDTO);

    MessageAcknowledgement messageAcknowledgementDTOToMessageAcknowledgement(MessageAcknowledgementDTO messageAcknowledgementDTO);

    @Mapping(target = "properties", ignore = true)
    JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage);

    @Mapping(target = "properties", ignore = true)
    JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO);

    UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO);

    UserMessageDTO userMessageToUserMessageDTO(UserMessage userMessage);

    PModeArchiveInfoDTO pModeArchiveInfoToPModeArchiveInfoDto(PModeArchiveInfo pModeArchiveInfo);

    PasswordEncryptionResultDTO passwordEncryptionResultToPasswordEncryptionResultDTO(PasswordEncryptionResult passwordEncryptionResult);

    DomibusPropertyMetadataDTO domibusPropertyMetadataToDomibusPropertyMetadataDTO(DomibusPropertyMetadata domibusPropertyMetadata);

    DomibusPropertyMetadata domibusPropertyMetadataDTOToDomibusPropertyMetadata(DomibusPropertyMetadataDTO domibusPropertyMetadata);

    ValidationIssueDTO pModeIssueToPModeIssueDTO(ValidationIssue validationIssue);

    PartyDTO partyToPartyDTO(Party party);

    Party partyDTOToParty(PartyDTO partyDTO);

    ProcessDTO processToProcessDTO(Process process);

    Process processDTOToProcess(ProcessDTO processDTO);

    PartyIdentifierDTO partyIdentifierToPartyIdentifierDto(Identifier partyIdentifier);

    TrustStoreDTO trustStoreEntryToTrustStoreDTO(TrustStoreEntry trustStoreEntry);

    TrustStoreEntry trustStoreDTOToTrustStoreEntry(TrustStoreDTO  trustStoreDTO);
}
