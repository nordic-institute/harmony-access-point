package eu.domibus.core.spi.validation;

/**
 * @author Cosmin Baciu
 * @since 5.0
 *
 * Exception raised in case the UserMessage validation does not pass
 */
public class UserMessageValidatorSpiException extends RuntimeException {

    public UserMessageValidatorSpiException() {
    }

    public UserMessageValidatorSpiException(String message) {
        super(message);
    }

    public UserMessageValidatorSpiException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserMessageValidatorSpiException(Throwable cause) {
        super(cause);
    }
}
