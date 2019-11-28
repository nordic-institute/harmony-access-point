package eu.domibus.ext.exceptions;
/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
public class DomibusMonitoringExtException extends DomibusServiceExtException {
    /**
     * Constructs a new instance with a specific error code and message.
     *
     * @param errorCode a DomibusErrorCode
     * @param message   the message detail.
     */
    public DomibusMonitoringExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    /**
     * Constructs a new instance with a specific error, message and cause.
     *
     * @param errorCode a DomibusError
     * @param message the message detail
     * @param throwable the cause of the exception
     */
    public DomibusMonitoringExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    /**
     * Constructs a new instance with a specific cause.
     *
     * @param cause the cause of the exception
     */
    public DomibusMonitoringExtException(Throwable cause) {
        super(DomibusErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
