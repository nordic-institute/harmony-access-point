package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class DomibusEncryptionException extends DomibusCoreException {

    public DomibusEncryptionException(DomibusCoreErrorCode dce, String message) {
        super(dce, message);
    }

    public DomibusEncryptionException(DomibusCoreErrorCode dce, String message, Throwable cause) {
        super(dce, message, cause);
    }

    public DomibusEncryptionException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }

    public DomibusEncryptionException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public DomibusEncryptionException(Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, cause.getMessage(), cause);
    }
}
