package eu.domibus.common.exception;

/**
 * This exceptions indicates the errors in sending and receiving of messages for the selected party in test Service UI.
 *
 * @author Soumya Chandran
 * @since 4.2
 */
public class TestServiceException extends Exception {

    public TestServiceException(String message) {
        super(message);
    }

    public TestServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TestServiceException() {
    }

    public TestServiceException(Throwable cause) {
        super(cause);
    }
}
