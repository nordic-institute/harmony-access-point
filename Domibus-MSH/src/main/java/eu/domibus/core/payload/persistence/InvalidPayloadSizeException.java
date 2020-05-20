package eu.domibus.core.payload.persistence;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

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
