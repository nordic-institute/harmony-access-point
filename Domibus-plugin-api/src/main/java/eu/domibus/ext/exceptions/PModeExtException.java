package eu.domibus.ext.exceptions;

/**
 * PMode upload/download operations Exception
 * @since 4.2
 * @author Catalin Enache
 */
public class PModeExtException extends DomibusServiceExtException {
    public PModeExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PModeExtException(Throwable cause) {
        super(DomibusErrorCode.DOM_003, "PMode operations Exception", cause);
    }
}
