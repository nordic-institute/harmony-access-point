package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class MessageNotFoundException extends MessagingException {

    public MessageNotFoundException(String messageId) {
        super(DomibusCoreErrorCode.DOM_009, "Message [" + messageId + "] does not exist", null);
    }

    public MessageNotFoundException(Long entityId) {
        super(DomibusCoreErrorCode.DOM_009, "Message entity Id [" + entityId + "] does not exist", null);
    }

    public MessageNotFoundException(String messageId, String context) {
        super(DomibusCoreErrorCode.DOM_009, "Message [" + messageId + "] does not exist for " + context, null);
    }

    private MessageNotFoundException(String messageId, String context, String details) {
        super(DomibusCoreErrorCode.DOM_009, details == null ? "Message [" + messageId + "] does not exist" + (context == null ? "" : " for " + context) : details, null);
    }

    public static MessageNotFoundException createMessageNotFoundException(String details) {
        return new MessageNotFoundException(null, null, details);
    }
}
