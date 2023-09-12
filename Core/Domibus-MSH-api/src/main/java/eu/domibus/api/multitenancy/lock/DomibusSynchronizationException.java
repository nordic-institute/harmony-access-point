package eu.domibus.api.multitenancy.lock;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class DomibusSynchronizationException extends DomibusCoreException {

    public DomibusSynchronizationException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusSynchronizationException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusSynchronizationException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusSynchronizationException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomibusSynchronizationException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
