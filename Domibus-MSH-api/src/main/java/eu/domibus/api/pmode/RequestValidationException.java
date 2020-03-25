package eu.domibus.api.pmode;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Class that encapsulates information about a pMode validation exception aka the list of issues
 */
public class RequestValidationException extends DomibusCoreException {

    public RequestValidationException(String message) {
        super(DomibusCoreErrorCode.DOM_001, message);
    }

    public RequestValidationException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }
}
