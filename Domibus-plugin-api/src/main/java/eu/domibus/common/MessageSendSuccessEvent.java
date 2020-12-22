package eu.domibus.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This event is used to transfer information on the message send success event, from the core to the plugins implementing PUSH methods.
 *
 * @author idragusa
 * @since 4.2
 */
public class MessageSendSuccessEvent implements Serializable, MessageEvent {

    protected String messageId;

    protected Map<String, String> properties = new HashMap<>(); //NOSONAR

    public MessageSendSuccessEvent(String messageId, Map<String, String> properties) {
        this.messageId = messageId;
        this.properties = properties;
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

    /**
     * Needed for backward compatibility between 4.2 and 5.0
     * @deprecated Use instead {@link MessageEvent#getProps()}
     */
    @Deprecated
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
