package eu.domibus.api.model;

/**
 * @author François Gautier
 * @since 5.0
 */
public class UserMessageDTO {

    final long entityId;

    final String messageId;

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
