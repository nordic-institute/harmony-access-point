package eu.domibus.api.model;

import eu.domibus.api.message.MessageSubtype;

import java.util.HashMap;
import java.util.Map;

/**
 * @author idragusa
 * @since 4.2
 */
public class UserMessageLogDto {

    public static final String MESSAGE_ID = "m_id";
    public static final String MESSAGE_SUBTYPE = "m_subtype";
    public static final String MESSAGE_BACKEND = "m_backend";
    public static final String PROP_VALUE = "p_value";
    public static final String PROP_NAME = "p_name";

    protected String messageId;

    protected MessageSubtype messageSubtype;

    protected String backend;

    private Map<String, String> properties = new HashMap<>();

    public UserMessageLogDto(String messageId, MessageSubtype messageSubtype, String backend) {
        this.messageId = messageId;
        this.messageSubtype = messageSubtype;
        this.backend = backend;
    }

    public UserMessageLogDto(Object[] tuple, Map<String, Integer> aliasToIndexMap) {
        this.messageId = (String) tuple[aliasToIndexMap.get(MESSAGE_ID)];
        Object subtype = tuple[aliasToIndexMap.get(MESSAGE_SUBTYPE)];
        if(subtype != null) {
            this.messageSubtype = MessageSubtype.valueOf((String) subtype);
        }
        this.backend = (String) tuple[aliasToIndexMap.get(MESSAGE_BACKEND)];
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageid(String messageId) {
        this.messageId = messageId;
    }

    public MessageSubtype getMessageSubtype() {
        return messageSubtype;
    }

    public void setMessageSubtype(MessageSubtype messageSubtype) {
        this.messageSubtype = messageSubtype;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public boolean isTestMessage() {
        if(MessageSubtype.TEST == messageSubtype) {
            return true;
        }
        return false;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
