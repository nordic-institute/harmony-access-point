package eu.domibus.ext.exceptions;


/**
 * @author François Gautier
 * @since 5.0
 */
public class DomibusDateTimeExtException extends DomibusServiceExtException {

    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode a DomibusErrorCode
     * @param message   the message detail.
     */
    public DomibusDateTimeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructs a new instance with a specific cause.
     *
     * @param message the message detail
     * @param cause the cause of the exception
     */
    public DomibusDateTimeExtException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_007, message, cause);
    }

}