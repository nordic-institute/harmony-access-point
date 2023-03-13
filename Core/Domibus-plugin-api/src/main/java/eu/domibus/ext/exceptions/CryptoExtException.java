package eu.domibus.ext.exceptions;

/**
 * Truststore get and upload operations Exception
 *
 * @author Soumya Chnadran
 * @since 5.1
 */
public class CryptoExtException extends DomibusServiceExtException {

    public CryptoExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CryptoExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    public CryptoExtException(Throwable cause) {
        this(DomibusErrorCode.DOM_001, cause.getMessage(), cause);
    }

    public CryptoExtException(String message) {
        this(DomibusErrorCode.DOM_001, message);
    }

    public CryptoExtException(String message, Throwable cause) {
        this(DomibusErrorCode.DOM_001, message, cause);
    }
}

