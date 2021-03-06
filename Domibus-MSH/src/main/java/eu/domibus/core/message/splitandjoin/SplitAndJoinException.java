package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class SplitAndJoinException extends DomibusCoreException {

    public SplitAndJoinException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public SplitAndJoinException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public SplitAndJoinException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public SplitAndJoinException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public SplitAndJoinException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
