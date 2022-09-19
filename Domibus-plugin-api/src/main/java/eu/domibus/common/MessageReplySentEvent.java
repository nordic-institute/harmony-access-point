package eu.domibus.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  This event is used to transfer information on the message reply sent event, from the core to the plugins implementing PUSH methods.
 *
 * @author Ion Perpegel
 * @since 5.0.1
 */
public class MessageReplySentEvent implements Serializable, MessageEvent {

    protected String messageId;
    protected Map<String, String> properties = new HashMap<>(); //NOSONAR
    protected long messageEntityId;

    public MessageReplySentEvent(String messageId) {
        this.messageId = messageId;
    }

    public MessageReplySentEvent(long messageEntityId, String messageId, Map<String, String> properties) {
        this.messageEntityId = messageEntityId;
        this.messageId = messageId;
        this.properties = properties;
    }

    @Override
    public Long getMessageEntityId() {
        return messageEntityId;
    }

    public void setMessageEntityId(long messageEntityId) {
        this.messageEntityId = messageEntityId;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public Map<String, String> getProps() {
        return Collections.unmodifiableMap(properties);
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
