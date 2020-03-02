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
}
