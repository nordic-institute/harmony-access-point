package eu.domibus.ext.exceptions;


/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class DomibusEArchiveExtException extends DomibusServiceExtException {
    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode a DomibusErrorCode
     * @param message   the message detail.
     */
    public DomibusEArchiveExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode a DomibusError
     * @param message the message detail
     * @param throwable the cause of the exception
     */
    public DomibusEArchiveExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    /**
     * Constructs a new instance with a specific cause.
     *
     * @param cause the cause of the exception
     */
    public DomibusEArchiveExtException(Throwable cause) {
        super(DomibusErrorCode.DOM_001, cause.getMessage(), cause);
    }

    /**
     * Constructs a new instance with a specific cause.
     *
     * @param message the message detail.
     */
    public DomibusEArchiveExtException(String message) {
        super(DomibusErrorCode.DOM_001, message);
    }
}