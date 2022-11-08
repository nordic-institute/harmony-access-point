package eu.domibus.ext.exceptions;

/**
 * Truststore get and upload operations Exception
 *
 * @author Soumya Chnadran
 * @since 5.1
 */
public class TruststoreExtException extends DomibusServiceExtException {

    public TruststoreExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public TruststoreExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    public TruststoreExtException(Throwable cause) {
        this(DomibusErrorCode.DOM_003, cause.getMessage(), cause);
    }

    public TruststoreExtException(String message) {
        this(DomibusErrorCode.DOM_003, message);
    }

}

