package eu.domibus.core.multitenancy;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class DomibusDomainException extends RuntimeException {

    public DomibusDomainException() {
    }

    public DomibusDomainException(String message) {
        super(message);
    }

    public DomibusDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusDomainException(Throwable cause) {
        super(cause);
    }

    public DomibusDomainException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
