package eu.domibus.ext.domain;

/**
 * Error DTO for REST API calls
 *
 * It will contains the generic error message and
 * additionally the validation issues for PMode / Parties operations
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class ErrorDTO {
    protected String message;

    public ErrorDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
