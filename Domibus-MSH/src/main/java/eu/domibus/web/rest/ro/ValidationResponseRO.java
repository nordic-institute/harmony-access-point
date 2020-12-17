package eu.domibus.web.rest.ro;

import eu.domibus.api.pmode.ValidationIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class ValidationResponseRO {

    private String message;

    private List<ValidationIssue> issues;

    public ValidationResponseRO(String message) {
        this(message, new ArrayList<>());
    }

    public ValidationResponseRO(String message, List<ValidationIssue> issues) {
        this.issues = issues;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<ValidationIssue> issues) {
        this.issues = issues;
    }


}
