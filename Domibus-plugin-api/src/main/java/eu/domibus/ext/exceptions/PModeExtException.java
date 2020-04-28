package eu.domibus.ext.exceptions;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * PMode upload/download operations Exception
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class PModeExtException extends DomibusServiceExtException {

    private List<String> validationIssues;

    public PModeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PModeExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    public PModeExtException(Throwable throwable) {
        this(DomibusErrorCode.DOM_003, throwable);
    }

    public PModeExtException(DomibusErrorCode errorCode, Throwable throwable) {
        super(errorCode, StringUtils.isNotBlank(throwable.getMessage()) ? throwable.getMessage() :
                "PMode operations Exception", throwable);
    }

    public void setValidationIssues(List<String> validationIssues) {
        this.validationIssues = validationIssues;
    }

    public String getErrorMessage() {
        Throwable cause = (this.getCause() == null ? this : this.getCause());
        String errorMessage = cause.getMessage();
        if (this.validationIssues != null) {
            errorMessage += ". Validation issues: " + String.join(",", validationIssues);

        }
        return errorMessage;
    }
}
