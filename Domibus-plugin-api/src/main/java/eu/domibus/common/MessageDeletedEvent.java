package eu.domibus.common;

import java.util.List;

/**
 * This event is used to notify the connector when messages are deleted.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageDeletedEvent {

    protected List<String> messageIds;

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }
}
