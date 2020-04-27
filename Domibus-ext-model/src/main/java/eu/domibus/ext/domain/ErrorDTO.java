package eu.domibus.ext.domain;

/**
 * Error DTO
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
