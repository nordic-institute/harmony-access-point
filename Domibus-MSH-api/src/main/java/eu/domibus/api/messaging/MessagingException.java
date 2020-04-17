package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
public class MessagingException extends DomibusCoreException {

    public MessagingException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_001, message, cause);
    }
}
