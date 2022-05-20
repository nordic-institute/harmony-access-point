package eu.domibus.common;

import java.util.List;

/**
 * This event is used to notify the connector when a batch of messages are deleted.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageDeletedBatchEvent {

    protected List<MessageDeletedEvent> messageDeletedEvents;

    public List<MessageDeletedEvent> getMessageDeletedEvents() {
        return messageDeletedEvents;
    }

    public void setMessageDeletedEvents(List<MessageDeletedEvent> messageDeletedEvents) {
        this.messageDeletedEvents = messageDeletedEvents;
    }
}
