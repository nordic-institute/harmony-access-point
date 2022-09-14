package eu.domibus.core.message;

public interface UserMessageContextKeyProvider {

    void setKeyOnTheCurrentMessage(String key, String value);

    String getKeyFromTheCurrentMessage(String key);

    void setObjectOnTheCurrentMessage(String key, Object value);

    Object getObjectFromTheCurrentMessage(String key);
}
