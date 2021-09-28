package eu.domibus.core.earchive;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception raised when dealing with eArchiving operations
 *
 * @author François Gautier
 * @since 5.0
 */
public class DomibusEArchiveException extends DomibusCoreException {

    public DomibusEArchiveException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusEArchiveException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }
}