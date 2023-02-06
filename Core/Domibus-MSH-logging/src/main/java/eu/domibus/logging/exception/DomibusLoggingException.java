package eu.domibus.logging.exception;

/**
 * @author Gabriel Maier
 * @since 5.1
 */
public class DomibusLoggingException extends RuntimeException {
    public DomibusLoggingException(String message, Throwable cause) {
        super(message, cause);
    }
}
