package eu.domibus.core.converter;

import eu.domibus.api.party.Party;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface PartyCoreMapper {

    @Mapping(target = "id", source = "entityId")
    eu.domibus.api.process.Process processToProcessAPI(Process process);

    @Mapping(ignore = true, target = "mepBinding")
    @Mapping(ignore = true, target = "initiatorPartiesXml")
    @Mapping(ignore = true, target = "responderPartiesXml")
    @Mapping(ignore = true, target = "initiatorParties")
    @Mapping(ignore = true, target = "responderParties")
    @Mapping(ignore = true, target = "legs")
    @InheritInverseConfiguration
    @WithoutAudit
    Process processAPIToProcess(eu.domibus.api.process.Process process);

    @Mapping(ignore = true, target = "processesWithPartyAsInitiator")
    @Mapping(ignore = true, target = "processesWithPartyAsResponder")
    Party configurationPartyToParty(eu.domibus.common.model.configuration.Party party);

    @WithoutAudit
    @Mapping(ignore = true, target = "password")
    eu.domibus.common.model.configuration.Party partyToConfigurationParty(Party party);

    @WithoutAuditAndEntityId
    eu.domibus.common.model.configuration.Identifier identifierToConfigurationIdentifier(eu.domibus.api.party.Identifier party);

    @WithoutAuditAndEntityId
    eu.domibus.common.model.configuration.PartyIdType partyIdTypeToConfigurationPartyIdType(eu.domibus.api.party.PartyIdType party);

    List<eu.domibus.common.model.configuration.Party> partyListToConfigurationPartyList(List<Party> partyList);

    List<PartyResponseRo> partyListToPartyResponseRoList(List<Party> partyList);

    @Mapping(ignore = true, target = "joinedIdentifiers")
    @Mapping(ignore = true, target = "joinedProcesses")
    @Mapping(ignore = true, target = "certificateContent")
    PartyResponseRo partyToPartyResponseRo(Party partyList);

    Party partyResponseRoToParty(PartyResponseRo partyList);

    List<Party> partyResponseRoListToPartyList(List<PartyResponseRo> partyResponseRoList);

    List<Party> configurationPartyListToPartyList(List<eu.domibus.common.model.configuration.Party> configurationPartyList);

    List<eu.domibus.api.process.Process> processListToProcessAPIList(List<Process> processList);

    List<eu.domibus.api.process.Process> processRoListToProcessAPIList(List<ProcessRo> processRoList);

    List<ProcessRo> processAPIListToProcessRoList(List<eu.domibus.api.process.Process> processList);

    @Mapping(target = "entityId", source = "id")
    ProcessRo processAPIToProcessRo(eu.domibus.api.process.Process process);

    @InheritInverseConfiguration
    eu.domibus.api.process.Process processRoToProcessAPI(ProcessRo process);

    TrustStoreRO trustStoreEntryToTrustStoreRO(TrustStoreEntry trustStoreEntry);

    TrustStoreEntry trustStoreROToTrustStoreEntry(TrustStoreRO trustStoreRO);

    List<TrustStoreRO> trustStoreEntryListToTrustStoreROList(List<TrustStoreEntry> trustStoreEntryList);
}
