package eu.domibus.ext.exceptions;

/**
 * Exception while clearing cache via Ext API
 *
 * @author Soumya Chandran
 * @since 5.0
 */
public class CacheExtServiceException extends DomibusServiceExtException {
    /**
     * @param errorCode
     * @param message
     */
    public CacheExtServiceException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * @param message error message
     * @param cause   cause of the Exception
     */
    public CacheExtServiceException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_004, message, cause);
    }

    /**
     * @param cause cause of the Exception
     */
    public CacheExtServiceException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

}
