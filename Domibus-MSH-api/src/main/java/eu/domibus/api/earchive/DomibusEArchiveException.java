package eu.domibus.api.earchive;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception raised when dealing with eArchiving operations
 *
 * @author François Gautier
 * @since 5.0
 */
public class DomibusEArchiveException extends DomibusCoreException {

    public DomibusEArchiveException(DomibusCoreErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public DomibusEArchiveException(DomibusCoreErrorCode code, String message) {
        super(code, message);
    }

    public DomibusEArchiveException(String message, Throwable cause) {
        this(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusEArchiveException(String message) {
        this(DomibusCoreErrorCode.DOM_001, message);
    }
}