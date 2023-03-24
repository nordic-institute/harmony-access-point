package eu.domibus.core.message;

public interface UserMessageContextKeyProvider {

    public static final String USER_MESSAGE = "userMessage";
    public static final String BACKEND_FILTER = "backendFilter";

    void setKeyOnTheCurrentMessage(String key, String value);

    String getKeyFromTheCurrentMessage(String key);

    void setObjectOnTheCurrentMessage(String key, Object value);

    Object getObjectFromTheCurrentMessage(String key);
}
