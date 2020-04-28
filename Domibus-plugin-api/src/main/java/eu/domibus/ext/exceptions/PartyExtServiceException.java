package eu.domibus.ext.exceptions;

/**
 * Exception while managing Ext Parties API
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class PartyExtServiceException extends PModeExtException {

    /**
     *
     * @param errorCode
     * @param message
     */
    public PartyExtServiceException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     *
     * @param message error message
     * @param cause cause of the Exception
     */
    public PartyExtServiceException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_003, message, cause);
    }

    /**
     *
     * @param cause cause of the Exception
     */
    public PartyExtServiceException(Throwable cause) {
        this(cause.getMessage(), cause);
    }
}
