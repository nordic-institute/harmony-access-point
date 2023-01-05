package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
public class DuplicateMessageFoundException extends MessagingException {

    public DuplicateMessageFoundException(String messageId) {
        super(DomibusCoreErrorCode.DOM_011, " Duplicate message Id found. [" + messageId + "] ", null);
    }

    public DuplicateMessageFoundException(String message, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_011, message, cause);
    }
}
