package eu.domibus.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This event is used to notify the connector when a batch of messages are deleted.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageDeletedBatchEvent implements MessageEvent, Serializable {

    private static final long serialVersionUID = 1L;
    protected List<MessageDeletedEvent> messageDeletedEvents;

    public List<MessageDeletedEvent> getMessageDeletedEvents() {
        return messageDeletedEvents;
    }

    public void setMessageDeletedEvents(List<MessageDeletedEvent> messageDeletedEvents) {
        this.messageDeletedEvents = messageDeletedEvents;
    }

    @Override
    public String getMessageId() {
        return null;
    }

    @Override
    public Long getMessageEntityId() {
        return null;
    }

    @Override
    public Map<String, String> getProps() {
        return null;
    }

    @Override
    public void addProperty(String key, String value) {

    }
}
