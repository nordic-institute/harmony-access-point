package eu.domibus.ext.services;

import eu.domibus.ext.domain.PartyDTO;

import java.util.List;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public interface PartyExtService {

    List<PartyDTO> getParties(String name,
                              String endPoint,
                              String partyId,
                              String processName,
                              int pageStart,
                              int pageSize);

}
