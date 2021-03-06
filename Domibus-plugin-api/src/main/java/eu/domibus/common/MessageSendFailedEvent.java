package eu.domibus.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * This event is used to transfer information on the failure to send message event, from the core to the plugins implementing PUSH methods.\
 *
 * @author idragusa
 * @since 4.2
 */
public class MessageSendFailedEvent implements Serializable {

    protected String messageId;

    protected Map<String, Object> properties = new HashMap<>(); //NOSONAR

    public MessageSendFailedEvent(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("messageId", messageId)
                .toString();
    }
}
