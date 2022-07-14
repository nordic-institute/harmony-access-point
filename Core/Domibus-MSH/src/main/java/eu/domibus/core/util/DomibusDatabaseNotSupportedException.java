package eu.domibus.core.util;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public class DomibusDatabaseNotSupportedException extends RuntimeException {

    public DomibusDatabaseNotSupportedException() {
    }

    public DomibusDatabaseNotSupportedException(String message) {
        super(message);
    }

    public DomibusDatabaseNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DomibusDatabaseNotSupportedException(Throwable cause) {
        super(cause);
    }

    public DomibusDatabaseNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

