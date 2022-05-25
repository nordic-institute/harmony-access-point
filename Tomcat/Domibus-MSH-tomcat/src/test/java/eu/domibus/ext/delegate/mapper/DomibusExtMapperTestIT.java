package eu.domibus.ext.delegate.mapper;

import eu.domibus.AbstractIT;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.party.Party;
import eu.domibus.api.process.Process;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.api.usermessage.domain.UserMessage;
import eu.domibus.ext.domain.*;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class DomibusExtMapperTestIT extends AbstractIT {

    @Autowired
    private DomibusExtMapper domibusExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void DomainToDomainDTO() {
        DomainDTO toConvert = (DomainDTO) objectService.createInstance(DomainDTO.class);
        final Domain converted = domibusExtMapper.domainDTOToDomain(toConvert);
        final DomainDTO convertedBack = domibusExtMapper.domainToDomainDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void JmsMessageToJmsMessageDTO() {
        JmsMessageDTO toConvert = (JmsMessageDTO) objectService.createInstance(JmsMessageDTO.class);
        final JmsMessage converted = domibusExtMapper.jmsMessageDTOToJmsMessage(toConvert);
        final JmsMessageDTO convertedBack = domibusExtMapper.jmsMessageToJmsMessageDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void UserMessageToUserMessageDTO() {
        UserMessageDTO toConvert = (UserMessageDTO) objectService.createInstance(UserMessageDTO.class);
        final UserMessage converted = domibusExtMapper.userMessageDTOToUserMessage(toConvert);
        final UserMessageDTO convertedBack = domibusExtMapper.userMessageToUserMessageDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void PartyToPartyDTO() {
        PartyDTO toConvert = (PartyDTO) objectService.createInstance(PartyDTO.class);
        final Party converted = domibusExtMapper.partyDTOToParty(toConvert);
        final PartyDTO convertedBack = domibusExtMapper.partyToPartyDTO(converted);
        // these fields are missing in PartyDTO, fill them so the assertion works
        convertedBack.setCertificateContent(toConvert.getCertificateContent());
        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void TrustStoreToTrustStoreDTO() {
        TrustStoreDTO toConvert = (TrustStoreDTO) objectService.createInstance(TrustStoreDTO.class);
        final TrustStoreEntry converted = domibusExtMapper.trustStoreDTOToTrustStoreEntry(toConvert);
        final TrustStoreDTO convertedBack = domibusExtMapper.trustStoreEntryToTrustStoreDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void partiesToPartiesDTO() {
        PartyDTO toConvert = (PartyDTO) objectService.createInstance(PartyDTO.class);
        final Party converted = domibusExtMapper.partyDTOToParty(toConvert);
        final List<PartyDTO> convertedBack = domibusExtMapper.partiesToPartiesDTO(Collections.singletonList(converted));
        // these fields are missing in PartyDTO, fill them so the assertion works
        convertedBack.get(0).setCertificateContent(toConvert.getCertificateContent());
        objectService.assertObjects(convertedBack.get(0), toConvert);
    }

    @Test
    public void processListToProcessesDTO() {
        ProcessDTO toConvert = (ProcessDTO) objectService.createInstance(ProcessDTO.class);
        final Process converted = domibusExtMapper.processDTOToProcess(toConvert);
        final List<ProcessDTO> convertedBack = domibusExtMapper.processListToProcessesDTO(Collections.singletonList(converted));

        objectService.assertObjects(convertedBack.get(0), toConvert);
    }
}