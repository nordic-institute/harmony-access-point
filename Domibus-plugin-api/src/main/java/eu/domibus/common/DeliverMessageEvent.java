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
    protected String finalRecipient;

    protected Map<String, String> properties = new HashMap<>(); //NOSONAR

    public DeliverMessageEvent(String messageId, String finalRecipient) {
        this.messageId = messageId;
        this.finalRecipient = finalRecipient;
    }

    public DeliverMessageEvent(String messageId, String finalRecipient, Map<String, String> properties) {
        this.messageId = messageId;
        this.finalRecipient = finalRecipient;
        this.properties = properties;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Needed for backward compatibility between 4.2 and 5.0
     * @deprecated Use instead {@link MessageEvent#getProps()}
     */
    @Deprecated
    public String getFinalRecipient() {
        return finalRecipient;
    }

    public void setFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
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
                .append("finalRecipient", finalRecipient)
                .toString();
    }
}
