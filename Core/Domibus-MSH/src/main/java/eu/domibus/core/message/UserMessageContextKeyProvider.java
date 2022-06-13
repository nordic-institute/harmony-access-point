package eu.domibus.core.message;

public interface UserMessageContextKeyProvider {

    void setKeyOnTheCurrentMessage(String key, String value);

    String getKeyFromTheCurrentMessage(String key);
}
