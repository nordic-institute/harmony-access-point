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

    public PModeExtException(Throwable cause) {
        this(DomibusErrorCode.DOM_003, cause.getMessage(), cause);
    }

    public PModeExtException(String message) {
        this(DomibusErrorCode.DOM_003, message);
    }

    public void setValidationIssues(List<String> validationIssues) {
        this.validationIssues = validationIssues;
    }

    /**
     * Return in a unique String both error message and validation issues
     * @return error message String
     */
    public String getErrorMessage() {
        Throwable cause = (this.getCause() == null ? this : this.getCause());
        String errorMessage = null;
        if (StringUtils.isNotBlank(cause.getMessage())) {
            errorMessage += cause.getMessage();
        }
        if (this.validationIssues != null) {
            errorMessage += ". Validation issues: " + String.join(",", validationIssues);

        }
        return errorMessage;
    }
}
