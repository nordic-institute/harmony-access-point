package eu.domibus.ext.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class ValidationResponseDTO {

    private String message;

    private List<ValidationIssueDTO> issues;

    public ValidationResponseDTO(String message) {
        this(message, new ArrayList<>());
    }

    public ValidationResponseDTO(String message, List<ValidationIssueDTO> issues) {
        this.issues = issues;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public List<ValidationIssueDTO> getIssues() {
        return issues;
    }

    public void setIssues(List<ValidationIssueDTO> issues) {
        this.issues = issues;
    }


}
