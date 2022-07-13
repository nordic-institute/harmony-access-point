package eu.domibus.api.messaging;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.MSHRole;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class MessageNotFoundException extends MessagingException {

    public MessageNotFoundException(String messageId) {
        super(DomibusCoreErrorCode.DOM_009, "Message [" + messageId + "] does not exist", null);
    }

    public MessageNotFoundException(String messageId, MSHRole mshRole) {
        super(DomibusCoreErrorCode.DOM_009, "Message [" + messageId + "]-[" + mshRole + "] does not exist", null);
    }

    public MessageNotFoundException(Long entityId) {
        super(DomibusCoreErrorCode.DOM_009, "Message entity Id [" + entityId + "] does not exist", null);
    }

    public MessageNotFoundException(String messageId, String context) {
        super(DomibusCoreErrorCode.DOM_009, "Message [" + messageId + "] does not exist for " + context, null);
    }
}
