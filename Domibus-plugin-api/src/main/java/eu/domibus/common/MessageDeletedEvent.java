package eu.domibus.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This event is used to notify the connector when a message is deleted.
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class MessageDeletedEvent implements MessageEvent {

    protected String messageId;
    protected Map<String, String> properties = new HashMap<>(); //NOSONAR
    protected long messageEntityId;

    @Override
    public long getMessageEntityId() {
        return messageEntityId;
    }

    public void setMessageEntityId(long messageEntityId) {
        this.messageEntityId = messageEntityId;
    }

    @Override
    public Map<String, String> getProps() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageEntityId", messageEntityId)
                .append("messageId", messageId)
                .append("properties", properties)
                .toString();
    }
}
