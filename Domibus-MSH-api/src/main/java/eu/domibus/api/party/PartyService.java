package eu.domibus.api.party;

import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.process.Process;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface PartyService {

    /**
     * Search the parties configured in the pmode. The search is made base on the following criterias.
     *
     * @param name        criteria to search on the the name of the party
     * @param endPoint    criteria to search on the endPoint of the party
     * @param partyId     criteria to search within the partyids of the party.
     * @param processName criteria to search party that are configured as initiator or responder in a process named like this criteria
     * @param pageStart   pagination start
     * @param pageSize    page size.
     * @return a list of party.
     */
    List<Party> getParties(String name,
                           String endPoint,
                           String partyId,
                           String processName,
                           int pageStart,
                           int pageSize);

    /**
     * Returns the list of Party Names for a specific Service and Action
     *
     * @param service Service name
     * @param action  Action name
     * @return List of Party names
     */
    List<String> findPartyNamesByServiceAndAction(final String service, final String action);

    /**
     * Returns the list of Party Names for a specific Service and Action configured as responders in process including
     * a push binding
     *
     * @param service Service name
     * @param action  Action name
     * @return List of Party names
     */
    List<String> findPushToPartyNamesByServiceAndAction(final String service, final String action);


    /**
     * Returns the Party Identifier Name for the gateway party
     *
     * @return Party Identifier Name
     */
    String getGatewayPartyIdentifier();

    /**
     * Updates the current pMode with the provided parties
     * @param partyList the list of parties to update as a snapshot
     * @param certificates the certificates as strings to be saved along the parties
     * @return a list of issues because the pMode is saved and validated
     * @throws PModeValidationException If there are validation errors, an exception is thrown
     */
    List<ValidationIssue> updateParties(List<Party> partyList, Map<String, String> certificates) throws PModeValidationException;

    /**
     * Retrieve all the processes configured in the pmode.
     *
     * @return a lit of processes.
     */
    List<Process> getAllProcesses();

    /**
     * Creates a {@code Party}
     *
     * @param party Party object
     * @param certificateContent certificate content in base64
     */
    void createParty(Party party, String certificateContent) throws PModeException;

    /**
     * Deletes a {@code Party}
     *
     * @param partyName
     * @throws PModeException
     */
    void deleteParty(final String partyName) throws PModeException;

    /**
     * Updates an existing Party
     *
     * @param party
     * @param certificateContent
     */
    void updateParty(Party party, String certificateContent) throws PModeException;
}
