package eu.domibus.ext.delegate.services.party;

import eu.domibus.api.party.PartyService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.services.PartyExtService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class PartyServiceDelegate implements PartyExtService {

    @Autowired
    PartyService partyService;

    @Autowired
    DomainExtConverter domainConverter;

    @Override
    public List<PartyDTO> getParties(String name,
                                     String endPoint,
                                     String partyId,
                                     String processName,
                                     int pageStart,
                                     int pageSize) {
        return domainConverter.convert(partyService.getParties(name, endPoint, partyId, processName, pageStart, pageSize), PartyDTO.class);
    }
}
