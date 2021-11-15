package eu.domibus.api.model;

/**
 * @author François Gautier
 * @since 5.0
 */
public class UserMessageDTO {

    long entityId;

    String messageId;

    public UserMessageDTO() {
        this(-1, null);
    }

    public UserMessageDTO(long entityId, String messageId) {
        this.entityId = entityId;
        this.messageId = messageId;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "UserMessageDto{" +
                "entityId=" + entityId +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}
