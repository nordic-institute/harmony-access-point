package eu.domibus.core.payload.persistence;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * Exception which is thrown in case Payload size exceeds the value defined in PMode
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class InvalidPayloadSizeException extends DomibusCoreException {

    public InvalidPayloadSizeException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public InvalidPayloadSizeException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public InvalidPayloadSizeException(String message) {
        super(DomibusCoreErrorCode.DOM_007, message);
    }
}
