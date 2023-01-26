package eu.domibus.api.crypto;

/**
 * @author François Gautier
 * @since 5.0
 */
public class KeyStoreContentDTO {

    private Long entityId;

    private byte[] content;

    public KeyStoreContentDTO(Long entityId, byte[] content) {
        this.entityId = entityId;
        this.content = content;
    }

    public Long getEntityId() {
        return entityId;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "TrustStoreContentDTO{" +
                "entityId=" + entityId +
                '}';
    }
}
