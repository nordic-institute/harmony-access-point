package eu.domibus.ext.exceptions;

/**
 * JMS service exception
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class JmsExtException extends DomibusServiceExtException {

    public JmsExtException(DomibusErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public JmsExtException(String message, Throwable e) {
        this(DomibusErrorCode.DOM_001, message, e);
    }

    public JmsExtException(DomibusErrorCode errorCode, String message, Throwable throwable) {
        super(errorCode, message, throwable);
    }

    public JmsExtException(Throwable cause) {
        this(DomibusErrorCode.DOM_001, cause.getMessage(), cause);
    }

    public JmsExtException(String message) {
        this(DomibusErrorCode.DOM_001, message);
    }

}
