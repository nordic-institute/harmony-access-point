package eu.domibus.core.util.xml;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception raised when dealing with XML related operations, like the creation of a MessageFactory
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusXMLException extends DomibusCoreException {

    public DomibusXMLException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusXMLException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusXMLException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusXMLException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomibusXMLException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}