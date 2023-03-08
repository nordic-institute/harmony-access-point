package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

/**
 * @author Soumya Chandran
 * @since 5.1
 */
public class DuplicateMessageFoundException extends MessagingException {

    public static final String DUPLICATE_MESSAGE_FOUND = "Duplicate message Id found.";
    public static final String DUPLICATE_MESSAGE_FOUND_SELF_SENDING = DUPLICATE_MESSAGE_FOUND + " For self sending please call the new method with two parameters: messageId and the access point role.";
    public DuplicateMessageFoundException(String messageId) {
        super(DomibusCoreErrorCode.DOM_011, DUPLICATE_MESSAGE_FOUND + "[" + messageId + "] ", null);
    }

    public DuplicateMessageFoundException(String messageId, Throwable cause) {
        super(DomibusCoreErrorCode.DOM_011, DUPLICATE_MESSAGE_FOUND_SELF_SENDING + "[" + messageId + "] ", cause);
    }
}
