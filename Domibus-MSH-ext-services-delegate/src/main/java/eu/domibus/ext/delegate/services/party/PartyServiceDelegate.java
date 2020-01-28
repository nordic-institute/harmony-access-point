package eu.domibus.ext.delegate.services.party;

import eu.domibus.api.party.Party;
import eu.domibus.api.party.PartyService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.services.PartyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@Service
public class PartyServiceDelegate implements PartyExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceDelegate.class);

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
        List<Party> parties = partyService.getParties(name, endPoint, partyId, processName, pageStart, pageSize);
        LOG.debug("Returned [{}] parties", parties.size());
        return domainConverter.convert(parties, PartyDTO.class);
    }
}
