package eu.domibus.api.model;

import org.apache.commons.lang3.BooleanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author idragusa
 * @since 4.2
 */
public class UserMessageLogDto {

    public static final String MESSAGE_ID = "m_id";
    public static final String TEST_MESSAGE = "m_test_message";
    public static final String MESSAGE_BACKEND = "m_backend";
    public static final String PROP_VALUE = "p_value";
    public static final String PROP_NAME = "p_name";

    protected String messageId;
    protected Boolean testMessage;
    protected String backend;

    private Map<String, String> properties = new HashMap<>();

    public UserMessageLogDto(String messageId, Boolean testMessage, String backend) {
        this.messageId = messageId;
        this.testMessage = testMessage;
        this.backend = backend;
    }

    public UserMessageLogDto(Object[] tuple, Map<String, Integer> aliasToIndexMap) {
        this.messageId = (String) tuple[aliasToIndexMap.get(MESSAGE_ID)];
        Object subtype = tuple[aliasToIndexMap.get(TEST_MESSAGE)];
        if(subtype != null) {
            this.testMessage = BooleanUtils.toBoolean((String) subtype);
        }
        this.backend = (String) tuple[aliasToIndexMap.get(MESSAGE_BACKEND)];
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageid(String messageId) {
        this.messageId = messageId;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public boolean isTestMessage() {
        return BooleanUtils.toBoolean(testMessage);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
