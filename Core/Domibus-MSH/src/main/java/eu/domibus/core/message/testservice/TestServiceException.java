package eu.domibus.core.message.testservice;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.common.ErrorCode;

/**
 * This exceptions indicates the errors in sending and receiving of messages for the selected party in test Service UI.
 *
 * @author Soumya Chandran
 * @since 4.2
 */
public class TestServiceException extends DomibusCoreException {


    /**
     * Constructs a new DomibusCoreException with a specific error and message.
     *
     * @param dce     a DomibusCoreErrorCode.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public TestServiceException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    /**
     * Constructs a new DomibusCoreException with a specific error, message and cause.
     *
     * @param dce     a DomibusCoreErrorCode.
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   the cause of the exceptions.
     */
    public TestServiceException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    /**
     * Constructs a new DomibusCoreException with a  error message.
     *
     * @param message the message detail. It is saved for later retrieval by the {@link #getMessage()} method.
     */
    public TestServiceException(String message) {
        super(message);
    }
}
