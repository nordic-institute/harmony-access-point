package eu.domibus.api.pmode;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Class that encapsulates information about a pMode validation exception aka the list of issues
 */
public class PModeValidationException extends PModeException {
    final static String PMODE_VALIDATION_ISSUES = "PMode validation failed";

    List<ValidationIssue> issues;

    public PModeValidationException(List<ValidationIssue> issues) {
        super(DomibusCoreErrorCode.DOM_003, PMODE_VALIDATION_ISSUES);

        this.issues = issues;
    }

    public PModeValidationException(String message, List<ValidationIssue> issues) {
        super(DomibusCoreErrorCode.DOM_003, message);

        this.issues = issues;
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }
}
