package eu.domibus.logging.exception;

public class DomibusLoggingException extends RuntimeException {
    public DomibusLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}
