package eu.domibus.ext.services;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.ProcessDTO;
import eu.domibus.ext.domain.TrustStoreDTO;
import eu.domibus.ext.exceptions.PartyExtServiceException;

import java.security.KeyStoreException;
import java.util.List;

/**
 * External service for Parties management
 *
 * @since 4.2
 * @author Catalin Enache
 */
public interface PartyExtService {

    /**
     * creates a {@code Party}
     *
     * @param partyDTO
     */
    void createParty(final PartyDTO partyDTO) throws PartyExtServiceException;

    /**
     * List all Parties
     *
     * @param name
     * @param endPoint
     * @param partyId
     * @param processName
     * @param pageStart
     * @param pageSize
     * @return List of {@link PartyDTO}
     */
    List<PartyDTO> getParties(String name,
                              String endPoint,
                              String partyId,
                              String processName,
                              int pageStart,
                              int pageSize);

    /**
     * Updates a Party
     *
     * @param partyDTO
     * @throws PartyExtServiceException
     */
    void updateParty(final PartyDTO partyDTO) throws PartyExtServiceException;

    /**
     * Deletes a {@code Party} identified by partyName
     * @param partyName name of the party
     */
    void deleteParty(final String partyName) throws PartyExtServiceException;

    /**
     * Returns the Party's certificate
     *
     * @param partyName
     * @return
     * @throws KeyStoreException
     */
    TrustStoreDTO getPartyCertificateFromTruststore(final String partyName) throws PartyExtServiceException;

    /**
     * Gets all processes
     *
     * @return List of {@link ProcessDTO}
     */
    List<ProcessDTO> getAllProcesses();
}
