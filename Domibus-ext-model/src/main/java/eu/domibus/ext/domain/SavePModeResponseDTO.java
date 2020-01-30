package eu.domibus.ext.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class SavePModeResponseDTO {

    private String message;

    private List<PModeIssueDTO> issues;

    public SavePModeResponseDTO(String message) {
        this(message, new ArrayList<>());
    }

    public SavePModeResponseDTO(String message, List<PModeIssueDTO> issues) {
        this.issues = issues;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public List<PModeIssueDTO> getIssues() {
        return issues;
    }

    public void setIssues(List<PModeIssueDTO> issues) {
        this.issues = issues;
    }


}
