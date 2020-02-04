package eu.domibus.ext.services;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.ProcessDTO;
import eu.domibus.ext.domain.TrustStoreDTO;

import java.security.KeyStoreException;
import java.util.List;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public interface PartyExtService {

    void createParty(final PartyDTO partyDTO);

    List<PartyDTO> getParties(String name,
                              String endPoint,
                              String partyId,
                              String processName,
                              int pageStart,
                              int pageSize);

    void deleteParty(final String partyName);

    TrustStoreDTO getPartyCertificateFromTruststore(final String partyName) throws KeyStoreException;

    List<ProcessDTO> getAllProcesses();
}
