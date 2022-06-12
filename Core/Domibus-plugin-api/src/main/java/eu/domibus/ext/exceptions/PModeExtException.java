package eu.domibus.ext.exceptions;

/**
 * PMode upload/download operations Exception
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class PModeExtException extends DomibusServiceExtException {

    public PModeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PModeExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    public PModeExtException(Throwable cause) {
        this(DomibusErrorCode.DOM_003, cause.getMessage(), cause);
    }

    public PModeExtException(String message) {
        this(DomibusErrorCode.DOM_003, message);
    }

}
