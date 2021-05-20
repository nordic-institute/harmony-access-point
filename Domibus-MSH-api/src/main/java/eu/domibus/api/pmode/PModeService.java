package eu.domibus.api.pmode;

import eu.domibus.api.pmode.domain.LegConfiguration;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface PModeService {

    LegConfiguration getLegConfiguration(String messageId) throws PModeException;

    byte[] getPModeFile(int id);

    PModeArchiveInfo getCurrentPMode();

    /**
     * Uploads a new pMode file that becames the current one
     * @param bytes The byte array representing the pMode content
     * @param description a description of the upload operation
     * @return a list of issues( notes or warnings) if any encountered
     * @throws PModeException In case there are any validation errors amongst the issues, an exception is raised
     */
    List<ValidationIssue> updatePModeFile(byte[] bytes, String description) throws PModeException;

    /**
     * Find the pmode configured name of a party from its partyId value and type.
     * @param partyId the party id value.
     * @param partyIdType the party type.
     * @return the party name.
     */
    String findPartyName(String partyId,String partyIdType);

    /**
     * Find the pmode configured name of an action from its value.
     * @param action the action value.
     * @return the action name.
     */
    String findActionName(String action);

    /**
     * Find the pmode configured name of a service from its value and type.
     * @param service the service value.
     * @param serviceType the service type.
     * @return the service name.
     */
    String findServiceName(String service, String serviceType);

    /**
     * Find the pmode configured name of an mpc from its value.
     * @param mpc the mpc value.
     * @return the mpc name.
     */
    String findMpcName(String mpc);


}
