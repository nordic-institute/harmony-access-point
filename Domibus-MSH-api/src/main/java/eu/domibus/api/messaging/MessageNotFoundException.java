package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class MessageNotFoundException extends MessagingException {

    public MessageNotFoundException(String message) {
        super(message, null);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
