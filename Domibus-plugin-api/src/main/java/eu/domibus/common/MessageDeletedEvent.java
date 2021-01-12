package eu.domibus.common;

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
}
