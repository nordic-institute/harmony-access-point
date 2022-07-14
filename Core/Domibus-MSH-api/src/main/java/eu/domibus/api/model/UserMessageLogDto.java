package eu.domibus.api.model;

import org.apache.commons.lang3.BooleanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author idragusa
 * @since 4.2
 */
public class UserMessageLogDto {

    public static final String ENTITY_ID = "m_entity_id";
    public static final String MESSAGE_ID = "m_id";
    public static final String TEST_MESSAGE = "m_test_message";
    public static final String MESSAGE_BACKEND = "m_backend";
    public static final String PROP_VALUE = "p_value";
    public static final String PROP_NAME = "p_name";

    protected Long entityId;
    protected String messageId;
    protected Boolean testMessage;
    protected String backend;
    protected MSHRole mshRole;

    private Map<String, String> properties = new HashMap<>();

    public UserMessageLogDto(Long entityId, String messageId, MSHRole mshRole) {
        this.entityId = entityId;
        this.messageId = messageId;
        this.mshRole = mshRole;
    }

    public UserMessageLogDto(Long entityId, String messageId, Boolean testMessage, String backend) {
        this.entityId = entityId;
        this.messageId = messageId;
        this.testMessage = testMessage;
        this.backend = backend;
    }

    public UserMessageLogDto(Object[] tuple, Map<String, Integer> aliasToIndexMap) {
        this.entityId = (Long) getObjectNullSafe(tuple, aliasToIndexMap, ENTITY_ID);
        this.messageId = (String) getObjectNullSafe(tuple, aliasToIndexMap, MESSAGE_ID);
        Object subtype = getObjectNullSafe(tuple, aliasToIndexMap, TEST_MESSAGE);
        if(subtype != null) {
            this.testMessage = (Boolean) subtype;
        }
        this.backend = (String) getObjectNullSafe(tuple, aliasToIndexMap, MESSAGE_BACKEND);
    }

    private Object getObjectNullSafe(Object[] tuple, Map<String, Integer> aliasToIndexMap, String entityId) {
        Integer integer = aliasToIndexMap.get(entityId);
        if(integer == null) {
            return null;
        }
        return tuple[integer];
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

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public void setMshRole(MSHRole mshRole) {
        this.mshRole = mshRole;
    }
}
