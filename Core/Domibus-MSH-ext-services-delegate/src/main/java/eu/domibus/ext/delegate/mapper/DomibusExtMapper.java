package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.process.Process;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.user.plugin.AuthenticationEntity;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.domain.*;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ioana Dragusanu (idragusa), azhikso
 * @since 4.1
 */
@Mapper(componentModel = "spring")
@DecoratedWith(DomibusExtMapperDecorator.class)
public interface DomibusExtMapper {

    Domain domainDTOToDomain(DomainDTO domain);

    DomainDTO domainToDomainDTO(Domain domain);

    @Mapping(target = "properties", ignore = true)
    JmsMessage jmsMessageDTOToJmsMessage(JmsMessageDTO jmsMessageDTO);

    @Mapping(target = "properties", ignore = true)
    JmsMessageDTO jmsMessageToJmsMessageDTO(JmsMessage jmsMessage);

    UserMessage userMessageDTOToUserMessage(UserMessageDTO userMessageDTO);

    UserMessageDTO userMessageToUserMessageDTO(UserMessage userMessage);

    Party partyDTOToParty(PartyDTO partyDTO);

    @Mapping(target = "certificateContent", ignore = true)
    PartyDTO partyToPartyDTO(Party party);

    TrustStoreEntry trustStoreDTOToTrustStoreEntry(TrustStoreDTO  trustStoreDTO);

    TrustStoreDTO trustStoreEntryToTrustStoreDTO(TrustStoreEntry trustStoreEntry);

    List<PartyDTO> partiesToPartiesDTO(List<Party> parties);

    List<ProcessDTO> processListToProcessesDTO(List<Process> processList);
    Process processDTOToProcess(ProcessDTO processDTO);

    PartInfoDTO partInfoToDto(PartInfo partInfo);

    PropertyDTO partPropertyToDto(PartProperty partProperty);

    default PartPropertiesDTO map(Set<PartProperty> partProperties) {
        if(partProperties == null) {
            return null;
        }
        PartPropertiesDTO result = new PartPropertiesDTO();
        Set<PropertyDTO> properties = new HashSet<>();
        for (PartProperty partProperty : partProperties) {
            properties.add(partPropertyToDto(partProperty));
        }
        result.setProperty(properties);



        return result;
    }

    AuthenticationEntity pluginUserDTOToAuthenticationEntity(PluginUserDTO pluginUserDTO);
}
