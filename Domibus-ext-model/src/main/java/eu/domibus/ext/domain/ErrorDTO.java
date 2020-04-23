package eu.domibus.ext.domain;

/**
 * @since 4.2
 * @author Catalin Enache
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
