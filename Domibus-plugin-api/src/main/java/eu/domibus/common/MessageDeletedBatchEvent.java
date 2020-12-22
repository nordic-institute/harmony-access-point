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

    protected List<String> messageIds;

    public List<MessageDeletedEvent> getMessageDeletedEvents() {
        return messageDeletedEvents;
    }

    public void setMessageDeletedEvents(List<MessageDeletedEvent> messageDeletedEvents) {
        this.messageDeletedEvents = messageDeletedEvents;
    }

    /**
     * Needed for backward compatibility between 4.2 and 5.0
     * @deprecated Use instead {@link MessageDeletedBatchEvent#getMessageDeletedEvents()}
     */
    @Deprecated
    public List<String> getMessageIds() {
        return messageIds;
    }

    /**
     * Needed for backward compatibility between 4.2 and 5.0
     * @deprecated Use instead {@link MessageDeletedBatchEvent#setMessageDeletedEvents(List)}
     */
    @Deprecated
    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }
}
