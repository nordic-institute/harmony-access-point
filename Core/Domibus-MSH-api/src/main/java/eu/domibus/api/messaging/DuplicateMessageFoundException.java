package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
public class DuplicateMessageFoundException extends MessagingException {

    public static final String DUPLICATE_MESSAGE_FOUND = "Duplicate message Id found.";

    public DuplicateMessageFoundException(String messageId) {
        super(DomibusCoreErrorCode.DOM_011, DUPLICATE_MESSAGE_FOUND + "[" + messageId + "] ", null);
    }

    public DuplicateMessageFoundException(String messageId, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_011, DUPLICATE_MESSAGE_FOUND + "[" + messageId + "] ", cause);
    }
}
