package eu.domibus.web.rest.ro;

import eu.domibus.api.pmode.PModeIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class SavePModeResponseRO {

    private String message;

    private List<PModeIssue> issues;

    public SavePModeResponseRO(String message) {
        this(message, new ArrayList<>());
    }

    public SavePModeResponseRO(String message, List<PModeIssue> issues) {
        this.issues = issues;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PModeIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<PModeIssue> issues) {
        this.issues = issues;
    }


}
