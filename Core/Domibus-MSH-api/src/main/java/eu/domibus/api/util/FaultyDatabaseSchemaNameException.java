package eu.domibus.api.util;

/**
 * @author Lucian FURCA
 * @since 5.1
 */
public class FaultyDatabaseSchemaNameException extends RuntimeException {

    public FaultyDatabaseSchemaNameException() {
    }

    public FaultyDatabaseSchemaNameException(String message) {
        super(message);
    }

    public FaultyDatabaseSchemaNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public FaultyDatabaseSchemaNameException(Throwable cause) {
        super(cause);
    }

    public FaultyDatabaseSchemaNameException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

