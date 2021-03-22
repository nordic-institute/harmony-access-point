package eu.domibus.common;

/**
 * This event is used to notify the connector when a message is deleted.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageDeletedEvent {

    protected String messageId;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
