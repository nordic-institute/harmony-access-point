package eu.domibus.api.property;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class DomibusPropertyException extends RuntimeException {

    public DomibusPropertyException() {
    }

    public DomibusPropertyException(String message) {
        super(message);
    }

    public DomibusPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusPropertyException(Throwable cause) {
        super(cause);
    }

    public DomibusPropertyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
