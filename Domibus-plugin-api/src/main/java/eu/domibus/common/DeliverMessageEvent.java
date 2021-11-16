package eu.domibus.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  This event is used to transfer information on the received message event, from the core to the plugins implementing PUSH methods.
 *
 * @author idragusa
 * @since 4.2
 */
public class DeliverMessageEvent implements Serializable, MessageEvent {

    protected String messageId;
    protected Map<String, String> properties = new HashMap<>(); //NOSONAR
    protected long messageEntityId;

    public DeliverMessageEvent(String messageId) {
        this.messageId = messageId;
    }

    public DeliverMessageEvent(long messageEntityId, String messageId, Map<String, String> properties) {
        this.messageEntityId = messageEntityId;
        this.messageId = messageId;
        this.properties = properties;
    }

    @Override
    public long getMessageEntityId() {
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
