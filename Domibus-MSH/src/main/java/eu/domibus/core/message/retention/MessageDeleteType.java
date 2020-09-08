package eu.domibus.core.message.retention;

/**
 * @author idragusa
 * @since 4.2
 */
public enum MessageDeleteType {
    DELETE_MESSAGE_ID_SINGLE("DELETE_MESSAGE_ID_SINGLE"),
    DELETE_MESSAGE_ID_MULTI("DELETE_MESSAGE_ID_MULTI");

    private String type;

    MessageDeleteType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
