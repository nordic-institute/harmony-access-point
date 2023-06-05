package eu.domibus.ext.exceptions;

/**
 * Exception raised while managing Plugin Users in EXT API
 *
 * @author Arun Raj
 * @since 5.0
 */
public class PluginUserExtServiceException extends DomibusServiceExtException {

    /**
     * @param errorCode
     * @param message
     */
    public PluginUserExtServiceException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * @param message error message
     * @param cause   cause of the Exception
     */
    public PluginUserExtServiceException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_001, message, cause);
    }

    /**
     * @param cause cause of the Exception
     */
    public PluginUserExtServiceException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

}
