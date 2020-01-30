package eu.domibus.api.pmode;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

import java.util.List;

public class PModeValidationException extends PModeException {
    final static String PMODE_VALIDATION_ISSUES = "PMode validation failed";

    List<PModeIssue> issues;

    public PModeValidationException(List<PModeIssue> issues) {
        super(DomibusCoreErrorCode.DOM_003, PMODE_VALIDATION_ISSUES);

        this.issues = issues;
    }

    public PModeValidationException(String message, List<PModeIssue> issues) {
        super(DomibusCoreErrorCode.DOM_003, message);

        this.issues = issues;
    }

    public List<PModeIssue> getIssues() {
        return issues;
    }
}
