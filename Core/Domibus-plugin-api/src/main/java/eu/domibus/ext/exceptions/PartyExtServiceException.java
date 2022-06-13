package eu.domibus.ext.exceptions;

/**
 * Exception while managing Ext Parties API
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class PartyExtServiceException extends DomibusServiceExtException {

    /**
     * @param errorCode
     * @param message
     */
    public PartyExtServiceException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * @param message error message
     * @param cause   cause of the Exception
     */
    public PartyExtServiceException(String message, Throwable cause) {
        super(DomibusErrorCode.DOM_004, message, cause);
    }

    /**
     * @param cause cause of the Exception
     */
    public PartyExtServiceException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

}
